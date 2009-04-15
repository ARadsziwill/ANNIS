<%
	response.setCharacterEncoding("UTF-8");
	response.setContentType("text/html; charset=UTF-8");
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Annis - Query Builder</title>


<link rel="stylesheet" type="text/css" href="javascript/extjs/resources/css/ext-all.css" />

<script type="text/javascript" src="javascript/extjs/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="javascript/extjs/ext-all.js"></script>

<!--[if IE]><script type="text/javascript" src="javascript/excanvas.js"></script><![endif]-->

<script type="text/javascript" src="javascript/annis/config.js"></script>

<script type="text/javascript" src="javascript/annis/querybuilder/lineDrawer.js"></script>
<script type="text/javascript" src="javascript/annis/querybuilder/queryBuilder.js"></script>

</head>
<body>
	<div id="container">
	    <div id="toolbar"></div>
		<div id="workspace">
			<canvas id="canvas" width="1000" height="1000"></canvas>
		</div>
	</div>	
</body>
</html>
