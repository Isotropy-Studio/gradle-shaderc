# gradle-shaderc

This gradle plugin declare a task: **shadercCompile**, to compile glsl shaders to SPIR-V binary files.


### Configuration

**gradle.properties**
```
org.gradle.daemon=false
```


**build.gradle**
```
plugins {
	id 'org.isotropy.gradleshaderc'
}

repositories {
	mavenCentral()
}

shadercCompile {
	inputDir = file("src/main/shader/")
	outputDir = file("src/main/resources/")
}
```
