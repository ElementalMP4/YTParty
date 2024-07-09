# YTParty

[![Project Status: Active](https://www.repostatus.org/badges/latest/active.svg)](https://www.repostatus.org/#active) [![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Watch YouTube with friends!

[Check us out here!](https://ytparty.voidtech.de/)

## Using YTParty

- Navigate to [YTParty](https://ytparty.voidtech.de/) and make an account if you don't already
- Optionally install the Chrome Extension (See below for guide)

### Creating a room without the Chrome extension

Once signed in, head over to [the create room page](https://ytparty.voidtech.de/html/createroom.html) and follow the room creation steps

### Creating a room with the Chrome extension

Once you have linked your account, simply navigate to a video you want to create your room from then press the YTParty logo to open up the extension and follow the on-screen prompts.

### Queueing videos without the Chrome extension

When you are in your player room, ensure you have clicked on the chat and then press `ctrl + m` to open up the player menu. From here, you can manipulate the queue.

### Queueing videos with the Chrome extension

Perform the same steps as you did to create a room with the extension, but this time simply press the add to queue button.

### How to add the Chrome extension

1.  1. Download or pull this repository
    2. Extract the zip folder of this repository if necessary

2. Navigate to the `extension/Chrome` folder. Copy all these files to a safe location such as `Documents/YTParty` so they do not get deleted.
3. In Chrome, navigate to `chrome://extensions` and enable developer mode
4. Click `Load Unpacked` and navigate to the `Documents/YTParty` folder (or wherever you put the extension files)
5. Click `Select Folder`. The extension should now be loaded.

## No FireFox extension?

I did make a FireFox extension, but I have since stopped developing it. This is due to the irritating nature of untrusted FireFox extension security and the difficulty of use for the end user. As such, I will not be returning to FireFox extension development for the forseeable future.

## Running YTParty

YTParty requires a config file named `config.properties` with the following fields:

```conf
#Hibernate Options
hibernate.User=YTParty
hibernate.Password=n0th1n9_t0_533_h3r3
hibernate.ConnectionURL=jdbc:postgresql://localhost:5432/YTParty
#Tomcat options
http.port=8080
#reCaptcha Token
captcha.Token=n0_h1dd3n_m355ag35
```

As well as a PostgreSQL server. Ensure that the database used in the connection URL exists on the PostgreSQL server. PGAdmin is recommended for database management.

## Pull Requests and Contributions

It is unlikely that I will accept PR's. This is due to my own concerns for security. If you have a suggestion or a problem, please [create a GitHub issue instead.](https://github.com/Elementalmp4/YTParty/issues/new)

## Technical Support

If you have any issues please [create a GitHub issue](https://github.com/Elementalmp4/YouTubeParty/issues/new)