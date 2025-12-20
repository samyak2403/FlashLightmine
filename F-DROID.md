# F-Droid Submission Guide

## Checklist

- [x] Open source license (MIT)
- [x] No proprietary dependencies
- [x] No tracking/analytics
- [x] No ads
- [x] Builds from source with Gradle
- [x] Metadata in `metadata/com.samyak2403.flashlightmine.yml`
- [x] Fastlane metadata in `fastlane/metadata/android/`
- [x] Screenshots provided
- [x] Changelog for each version

## Submission Steps

1. Fork [fdroiddata](https://gitlab.com/fdroid/fdroiddata)
2. Copy `metadata/com.samyak2403.flashlightmine.yml` to the fork
3. Create a merge request
4. Wait for review and build verification

## Build Verification

```bash
# Test local build
./gradlew clean assembleRelease

# Verify no proprietary dependencies
./gradlew app:dependencies
```

## Anti-Features

This app has NO anti-features:
- No ads
- No tracking
- No non-free dependencies
- No non-free assets
