/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.frontend.servlets;

import annis.exceptions.AnnisServiceFactoryException;
import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisBinary;
import annis.service.ifaces.AnnisBinaryMetaData;
import java.rmi.RemoteException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.*;

import java.io.IOException;
import java.util.Map;

/**
 * This Servlet provides binary-files with a stream of partial-content. The
 * first GET-request is answered with the status-code 206 Partial Content.
 * 
 * @author benjamin
 * 
 */
public class BinaryServlet extends HttpServlet
{

  private static final long serialVersionUID = -8182635617256833563L;
  private int slice = 200000; // max portion which is transfered over rmi
  private long corpusId;

  @SuppressWarnings("deprecation")
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {

    // get Parameter from url, actually it' s only the corpusId
    Map<String, String[]> binaryParameter = request.getParameterMap();
    corpusId = Long.parseLong(binaryParameter.get("id")[0]);
    ServletOutputStream out = response.getOutputStream();

    try
    {

      String range = request.getHeader("Range");
      AnnisService service = AnnisServiceFactory.getClient(this.getServletContext().
              getInitParameter("AnnisRemoteService.URL"));

      if (range != null)
      {
        responseStatus206(service, out, response, range);
      } else
      {

        responseStatus200(service, out, response);
      }
    } catch (AnnisServiceFactoryException e)
    {
      throw new RuntimeException(e.getMessage());
    }

    out.flush();
    out.close();
  }

  private void responseStatus206(AnnisService service, ServletOutputStream out,
          HttpServletResponse response, String range) throws RemoteException, IOException
  {
    AnnisBinaryMetaData bm = service.getBinaryMeta(corpusId);
    AnnisBinary binary;

    // Range: byte=x-y | Range: byte=0-
    String[] rangeTupel = range.split("-");
    int offset = Integer.parseInt(rangeTupel[0].split("=")[1]);

    if (rangeTupel.length > 1)
    {
      slice = Integer.parseInt(rangeTupel[1]);
    } else
    {
      slice = bm.getLength();
    }


    binary = service.getBinary(corpusId, offset + 1, slice - offset);

    response.setHeader("Content-Range", "bytes " + offset + "-"
            + (bm.getLength() - 1) + "/" + bm.getLength());
    response.setContentType(bm.getMimeType());
    response.setStatus(206);
    response.setContentLength(binary.getBytes().length);

    out.write(binary.getBytes());
  }

  private void responseStatus200(AnnisService service, ServletOutputStream out,
          HttpServletResponse response) throws RemoteException, IOException
  {
    AnnisBinaryMetaData binaryMeta = service.getBinaryMeta(corpusId);

    response.setStatus(200);
    response.setHeader("Accept-Ranges", "bytes");
    response.setContentType(binaryMeta.getMimeType());
    response.setHeader("Content-Range", "bytes 0-" + (binaryMeta.getLength() - 1)
            + "/" + binaryMeta.getLength());
    response.setContentLength(binaryMeta.getLength());

    getCompleteFile(service, out);
  }

  /**
   * This function get the whole binary-file and put it to responds.out
   * there must exist at least one byte
   * 
   * 
   * @param service
   * @param out
   * @param corpusId 
   */
  private void getCompleteFile(AnnisService service, ServletOutputStream out)
          throws RemoteException, IOException
  {

    AnnisBinaryMetaData annisBinary = service.getBinaryMeta(corpusId);
    slice = annisBinary.getLength();

    out.write(service.getBinary(corpusId, 1, annisBinary.getLength() - 1).getBytes());
  }
}
