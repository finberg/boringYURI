/*
 * Copyright 2020 Anton Novikau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.vanniktech.maven.publish.tasks.SourcesJar
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    kotlin("jvm")
    kotlin("kapt")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

val fatJarMembers: Set<String> = setOf(
    "processor-common",
    "processor-common-apt",
    "processor-steps",
)

fatJarMembers.forEach {
    evaluationDependsOn(":$it")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":api"))
    fatJarMembers.forEach { projectName ->
        compileOnly(project(":$projectName"))
    }

    //noinspection AnnotationProcessorOnCompilePath
    compileOnly(libs.google.auto.service)
    kapt(libs.google.auto.service)

    implementation(libs.square.javaPoet)
    implementation(libs.androidx.room.xprocessing)
    implementation(libs.commons.text)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.jar.configure {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(
        configurations.compileClasspath.get()
            .filter { it.extension == "jar" && it.nameWithoutExtension in fatJarMembers }
            .map {
                zipTree(it)
            }
    )
}

tasks.withType(SourcesJar::class)
    .configureEach {
        if (name == "javaSourcesJar") {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE

            fatJarMembers.forEach {
                from(
                    project(":$it").sourceSets.getByName("main").java.srcDirs
                )
            }
        }
    }

tasks.withType<DokkaTask>().configureEach {
    mustRunAfter(tasks["kaptKotlin"])

    dokkaSourceSets {
        configureEach {
            fatJarMembers.forEach {
                sourceRoots.from(
                    project(":$it").sourceSets.getByName("main").java.srcDirs
                )
            }
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString() // in order to compile Kotlin to java 11 bytecode
    }
}
