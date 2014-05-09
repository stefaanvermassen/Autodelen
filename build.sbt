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
  "org.webjars"             % "jquery-ui"                    % "1.10.3",
  "org.webjars"             % "bootstrap-datetimepicker"     % "2.2.0", // Timepicker for bootstrap v3
  "org.mindrot"             % "jbcrypt"                      % "0.3m", // Library for secure password storage
  "com.typesafe"            %% "play-plugins-mailer"         % "2.1-RC2",
  "org.webjars"             % "leaflet"                      % "0.7.2", // Library for maps
  "mysql"                   % "mysql-connector-java"         % "5.1.29",
  "com.typesafe.akka"       %% "akka-actor"                  % "2.2.0"
)

play.Project.playJavaSettings
