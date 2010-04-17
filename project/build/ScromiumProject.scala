import sbt._

class ScromiumProject(info : ProjectInfo) extends DefaultProject(info) with BasicScalaIntegrationTesting {
  override def compileOptions = Deprecation :: Unchecked :: super.compileOptions.toList
  
  val sourceArtifact = Artifact(artifactID, "src", "jar", Some("sources"), Nil, None) 
  val docsArtifact = Artifact(artifactID, "docs", "jar", Some("javadocs"), Nil, None) 
  override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageDocs, packageSrc)
  
  val codaRepo = "Coda Hale's Repository" at "http://repo.codahale.com/"
  val jetlangRepo = "Jet Lang Repository" at "http://jetlang.googlecode.com/svn/repo/"
  
  val metrics = "com.yammer" % "metrics_2.8.0.Beta1" % "1.0.2" withSources()
  val guild = "com.codahale" % "guild_2.8.0.Beta1" % "1.0-SNAPSHOT" withSources()
  val jetlang = "org.jetlang" % "jetlang" % "0.2.0" withSources()
  val pool = "commons-pool" % "commons-pool" % "1.5.4" withSources() intransitive()
  val slf4japi = "org.slf4j" % "slf4j-api" % "1.5.11" withSources() intransitive()
  val slf4j = "org.slf4j" % "slf4j-jdk14" % "1.5.11" withSources() intransitive()
  
  val mockito = "org.mockito" % "mockito-all" % "1.8.1" % "test" withSources()
}
