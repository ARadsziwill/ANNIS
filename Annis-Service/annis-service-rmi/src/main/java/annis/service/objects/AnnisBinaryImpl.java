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
package annis.service.objects;

import annis.service.ifaces.AnnisBinary;

public class AnnisBinaryImpl implements AnnisBinary
{

  private static final long serialVersionUID = -4484371544441543151L;
  private byte[] bytes;
  private long id;
  private String mimeType;
  private String fileName;
  private int length;

  @Override
  public byte[] getBytes()
  {
    return this.bytes;
  }

  @Override
  public long getId()
  {
    return this.id;
  }

  @Override
  public void setBytes(byte[] bytes)
  {
    if (bytes == null)
    {
      throw new RuntimeException("didn't get bytes");
    }
    this.bytes = bytes;
  }

  @Override
  public void setId(long id)
  {
    this.id = id;
  }

  @Override
  public String getJSON()
  {
    return "{id: " + this.id + ", mimeType: '" + this.mimeType + ", bytes: '"
            + new String(this.bytes) + "'}";
  }

  @Override
  public String getMimeType()
  {
    return this.mimeType;
  }

  @Override
  public void setMimeType(String mimeType)
  {
    this.mimeType = mimeType;
  }

  @Override
  public String getFileName()
  {
    return this.fileName;
  }

  @Override
  public void setFileName(String fileName)
  {
    this.fileName = fileName;
  }

  @Override
  public void setLength(int length)
  {
    this.length = length;
  }

  @Override
  public int getLength()
  {
    return this.length;
  }
}
