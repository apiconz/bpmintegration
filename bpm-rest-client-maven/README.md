Para que este proyecto funcione es necesario agregar a nuestro repositorio de dependencias el jar **[com.ibm.ws.runtime.jar]** que se encuentra dentro de la instalación del portal.

Se ejecuta la siguiente sentencia para agregar dicho jar a nuestro repositorio
[mvn install:install-file -Dfile=d:\temp\jars\com.ibm.ws.runtime.jar -DgroupId=com.ibm.ws.runtime -DartifactId=runtime -Dversion=1.0 -Dpackaging=jar ]

Se agregará luego la siguiente declaración en nuestro archivo POM.
[
		<dependency>
			<groupId>com.ibm.ws.runtime</groupId>
			<artifactId>runtime</artifactId>
			<version>1.0</version>
			<scope>provided</scope>
		</dependency>
]