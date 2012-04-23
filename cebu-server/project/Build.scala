import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "cebu-server"
    val appVersion      = "1.0-SNAPSHOT"


//    val appDependencies = Seq(
//      "net.sf.opencsv" % "opencsv" % "2.0",
//      "gov.sandia.foundry" % "gov-sandia-cognition-common-core" % "3.3.2",
//      "org.geotools" % "gt-main" % "8.0-M4",
//      "org.geotools" % "gt-epsg-hsql" % "8.0-M4"
//      
//    )

//    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
    val main = PlayProject(appName, appVersion, mainLang = JAVA).settings(
      // Add your own project settings here      
    )

}
