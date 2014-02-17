name := "Autodelen"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache
)

libraryDependencies ++= Seq(
  "org.webjars"             %% "webjars-play"                % "2.2.0",
  "org.webjars"             %  "bootstrap"                   % "3.1.1",
  "org.mindrot"             % "jbcrypt"                       % "0.3m", // Library for secure password storage
  "mysql"                   % "mysql-connector-java"      % "5.1.29"
)

play.Project.playJavaSettings
