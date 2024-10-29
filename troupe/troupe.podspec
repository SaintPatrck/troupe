Pod::Spec.new do |spec|
    spec.name                     = 'troupe'
    spec.version                  = '1.0.0'
    spec.homepage                 = 'https://github.com/saintpatrck/troupe'
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'Timber style logging for KMP'
    spec.vendored_frameworks      = 'build/cocoapods/framework/troupe.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target = '16.0'
                
                
    if !Dir.exist?('build/cocoapods/framework/troupe.framework') || Dir.empty?('build/cocoapods/framework/troupe.framework')
        raise "

        Kotlin framework 'troupe' doesn't exist yet, so a proper Xcode project can't be generated.
        'pod install' should be executed after running ':generateDummyFramework' Gradle task:

            ./gradlew :troupe:generateDummyFramework

        Alternatively, proper pod installation is performed during Gradle sync in the IDE (if Podfile location is set)"
    end
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':troupe',
        'PRODUCT_MODULE_NAME' => 'troupe',
    }
                
    spec.script_phases = [
        {
            :name => 'Build troupe',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
                  exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
                
end
