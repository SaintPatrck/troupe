import org.jetbrains.kotlin.konan.properties.loadProperties
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kover)
    id("maven-publish")
}

val localProperties: Properties? by lazy {
    try {
        loadProperties("local.properties")
    } catch (e: Throwable) {
        null
    }
}

group = "com.saintpatrck.logging"
version = "0.0.1"

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
        publishLibraryVariantsGroupedByFlavor = true
        compilations.all {
            kotlinOptions {
                jvmTarget = libs.versions.jvm.target.get()
            }
        }
    }

    jvmToolchain {
        languageVersion.set(
            JavaLanguageVersion.of(libs.versions.jvm.toolchain.get().toInt())
        )
    }

    applyDefaultHierarchyTemplate()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Timber style logging for KMP projects"
        homepage = "https://github.com/saintpatrck/troupe"
        version = "${project.version}"
        ios.deploymentTarget = libs.versions.ios.deploymentTarget.get()
        framework {
            baseName = project.name
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.bundles.android.unitTest)
            }
        }
    }
}

android {
    namespace = "$group.${project.name}"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kover {
    currentProject {
        sources {
            excludeJava = true
        }
    }
    reports {
        filters {
            excludes {
                androidGeneratedClasses()
            }
        }
        total {
            xml {
                onCheck.set(true)
            }
            html {
                onCheck.set(true)
            }
        }
    }
}

configure<PublishingExtension> {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/saintpatrck/troupe")
            credentials {
                username = localProperties?.getProperty("gpr.user")
                    ?: System.getenv("GPR_USERNAME")?.takeUnless { it.isEmpty() }

                password =
                    localProperties?.getProperty("gpr.password")
                        ?: System.getenv("GPR_PASSWORD")?.takeUnless { it.isEmpty() }
            }
        }
    }
    publications {
        register<MavenPublication>(name = "Troupe") {
            from(components["kotlin"])
        }
    }
}
