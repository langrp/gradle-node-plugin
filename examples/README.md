# Example Projects
Projects demonstrate usage of angular plugin. The required gradle version is 5.6 or higher. To initialize gradle wrapper
execute `gradle wrapper`.
Each project is independent from all other projects, but all projects use shared NodeJS and npm from root project.

## Sample Angular Application
The example project shows initialization of angular project from only gradle installation.
```shell script
./gradlew :sample-app:angularInit --style=scss --routing --skipGit
```

Start angular demo application with:
```shell script
cd sample-app
./ng serve
```

## Multiple Project Configuration
This example demonstrates build of angular application along with spring backend application build. The components are:
* components - Angular library with shared components/modules.
* resources-ng - Main angular project to compile and wrap static resources into jar file. This build depends on
 other project `components` (dependency can't be resolved until project is initialized).
* resources-view - Spring web-flux application to serve our static angular resources

Execute following lines to initialize project
```shell script
./gradlew :multi-project:angularInit --style=scss --routing --skipGit --mainProject=resources-ng
./gradlew :multi-project:angularCli --cmd=generate --args="library components"
```

Since angular project was not initialized defined dependency in `resources-ng` has condition on presence of
angular.json. Once the project is initialized this dependency would be resolvable. Build the project main access point:

```shell script
 ./gradlew :multi-project:resources-view:build
```

You can notice angular `components` are being compiled first followed by `resources-ng` project. Due to dependency
between these projects, `node_modules` directory now contains resolved dependency of `components` under
node_modules/@sample. Notice the group from angular extension is used here.

Run and open project on [http://localhost:8080/index.html](http://localhost:8080/index.html).
```shell script
java -jar multi-project/resources-view/build/libs/resources-view-1.0.0-SNAPSHOT.jar
```
