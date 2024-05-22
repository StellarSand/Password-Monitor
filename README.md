<img src="fastlane/metadata/android/en-US/images/icon.png" width="80" alt="App icon"/> 

# Password Monitor

Ensure your password safety by scanning for potential breaches.

**Will be available on F-Droid very soon :)**

<img src="https://img.shields.io/f-droid/v/com.password.monitor?logo=FDroid&color=green&style=for-the-badge" alt="F-Droid Version"> <img src="https://img.shields.io/endpoint?url=https://play.cuzi.workers.dev/play?i=com.password.monitor&m=$version&logo=GooglePlay&color=3BCCFF&label=Google%20Play&style=for-the-badge" alt="Google Play Version"> <img src="https://img.shields.io/github/v/release/StellarSand/Password-Monitor?logo=GitHub&color=212121&label=GitHub&style=for-the-badge" alt="GitHub Version">



## Contents
- [Overview](#overview)
- [Features](#features)
- [Screenshots](#screenshots)
- [Download](#download)
- [Changelog](#changelog)
- [Ensuring the privacy of your passwords](#ensuring-the-privacy-of-your-passwords)
- [Privacy Policy](#privacy-policy)
- [Issues](#issues)
- [Contributing](#contributing)
- [Credits](#credits)
- [License](#license)



## Overview
This app seamlessly integrates with [Have I Been Pwned?](https://haveibeenpwned.com) to help you verify whether your passwords have been compromised in any publicly disclosed data breaches.
<br>Your passwords stay private and are never shared anywhere. Check [ensuring the privacy of your passwords](#ensuring-the-privacy-of-your-passwords)



## Features
- Fully open source
- Material design 3 & Material You
- Supports both light and dark theme
- No ads
- No collection of personal data
- Supported languages: 
   - English
   - Chinese
   - Dutch
   - French
   - German
   - Italian
   - Japanese
   - Spanish
   - Swedish
   - Turkish



## Screenshots
<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="200"/>  <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="200"/>

<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="200"/>  <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="200"/>



## Download
**Disclaimer**: The Google Play account is not owned by me.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
alt="Get it on F-Droid"
height="80">](https://f-droid.org/packages/com.password.monitor)
[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"
alt="Get it on Google Play"
height="80">](https://play.google.com/store/apps/details?id=com.password.monitor)
[<img src="https://raw.githubusercontent.com/Kunzisoft/Github-badge/main/get-it-on-github.png"
alt="Get it on GitHub"
height="80">](https://github.com/StellarSand/Password-Monitor/releases/latest)

### Verify integrity if downloaded from GitHub
To verify the integrity of the `.apk`/`.aab` files, if downloaded from GitHub, perform the following steps:

<details>
  <summary><b>Windows</b></summary>

1. Open Powershell by searching for it in the `Start menu` OR by pressing `Win + R` and typing `powershell`
2. Change directory to the downloaded path
   ```
   cd "C:\path\to\downloaded\file"
   ```
   Example:
   ```
   cd "C:\Users\JohnDoe\Downloads"
   ```
3. Compute the SHA-256 Hash
   ```
   Get-FileHash -Algorithm SHA256 -Path "filename"
   ```
   Example:
   ```
   Get-FileHash -Algorithm SHA256 -Path "PasswordMonitor_v1.5.0.apk"
   ```
4. The computed hash value should be exactly the same as the one provided in the `.sha256` file on GitHub.
</details>

<details>
  <summary><b>Linux & macOS</b></summary>

1. Open terminal
2. Change directory to the downloaded path
   ```
   cd /path/to/downloaded/file
   ```
   Example:
   ```
   cd /home/JohnDoe/Downloads/
   ```
3. Compute the SHA-256 Hash
   ```
   sha256sum filename
   ```
   Example:
   ```
   sha256sum PasswordMonitor_v1.5.0.apk
   ```
4. The computed hash value should be exactly the same as the one provided in the `.sha256` file on GitHub.
</details>



## Changelog
All notable changes are documented in the [changelog](https://github.com/StellarSand/Password-Monitor/blob/master/CHANGELOG.md).



## Ensuring the privacy of your passwords
For a detailed explanation on how your passwords are kept private, refer to the [wiki](https://github.com/StellarSand/Password-Monitor/wiki).



## Privacy Policy
Privacy policy is located [here](https://github.com/StellarSand/Password-Monitor/blob/master/PRIVACY.md).



## Issues
If you find bugs or have suggestions, please report it to the [issue tracker](https://github.com/StellarSand/Password-Monitor/issues). 

Please search for existing issues before opening a new one. Any duplicates will be closed immediately.



## Contributing
Please read the [contributing guidelines](https://github.com/StellarSand/Password-Monitor/blob/main/CONTRIBUTING.md) before contributing.

New pull requests can be submitted [here](https://github.com/StellarSand/Password-Monitor/pulls).



## Credits
- [Troy Hunt](https://github.com/troyhunt) & [Cloudflare](https://www.cloudflare.com/) for the Have I Been Pwned API.
- [parveshnarwal](https://github.com/parveshnarwal) for publishing the app on Google Play.



## License
This project is licensed under the terms of [GPL v3.0 license](https://github.com/StellarSand/Password-Monitor/blob/main/LICENSE).
