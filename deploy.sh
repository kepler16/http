mvn deploy:deploy-file \
		-DrepositoryId=github \
		-Durl=https://maven.pkg.github.com/kepler16/http \
		-Dfile=target/lib.jar \
		-DpomFile=pom.xml

# "-Dtoken=asdfadf"
