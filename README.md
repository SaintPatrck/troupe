# Troupe

## About Troupe

### What is Troupe?

Troupe is a Kotlin Multi-Platform (KMP) port of [Timber](https://github.com/JakeWharton/timber),
renamed as a homage to Bards from [D&D](https://dnd.wizards.com/) 🐉 :game_die:.

### Why create Troupe?

I wanted centralized and uniform logging across all of my other KMP libraries as well as my
native mobile, web, and desktop applications.

### Why port Timber?

As a user and advocate of [Timber](https://github.com/JakeWharton/timber) it seemed natural to use
it as inspiration. Luckily for me it was already written in Kotlin so the core source of the library
is the same. Minor tweaks were made to remove Android/JVM specific APIs so that it could be written
in `commonMain`.

### What's with the name?
It's a mix of honoring Timber's pun-y naming and my love for D&D. `Bard`s (`Tree`s) are usually
responsible for reciting stories like their adventuring counterparts. Since a `Bard` rarely
travels alone and works best with a network of other `Bard`s the library name was chosen to
represent "a group of dancers, actors, or other entertainers who tour to
different venues.", or *Troupe*[^fn1]

# Usage

## Installation

### Maven projects (KMP/JVM/Android)
```kotlin
depedencies {
    // For KMP projects
    implementation("com.saintpatrck.logging:troupe:$version")
    
    // Android only artifacts
    implementation("com.saintpatrck.logging:troupe-android:$version")
    
    // JVM only artifacts
    implementation("com.saintpatrck.logging:troupe-jvm:$version")
}
```

## Subscribe to logs
Subscribe to logs by implementing `Bard` and recruiting it to the Troupe

### KMP/JVM/Android
```kotlin
class MyBard : Bard() {
    override fun sing(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        // Handle log as desired.
    }
}
```

### MacOS/iOS
TBD

## Generate logs
### KMP/JVM/Android
```kotlin
// Generate DEBUG log
Troupe.d()

// Generate VERBOSE log
Troupe.v()

// Generate INFO log
Troupe.i()

// Generate WARN log
Troupe.w()

// Generate ERROR log
Troupe.e()

// Generate WTF log
Troupe.wtf()
```

### XCode projects(MacOS/iOS)
TBD

[^fn1]: [Oxford Languages](https://languages.oup.com/google-dictionary-en/)
