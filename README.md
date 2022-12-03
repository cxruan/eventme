## Sprint additions
### Added features
- Saved Events: Logged in users will be able to save events by clicking the save button on the event box and the event registration page. This will allow users to save events to view later without having to register. Users will be able to see their saved events on their profile page. 
- Edit Profile Functionality: Logged in users will be able to edit details of their profile such as their name and their birthday.
- Navigation Button: Users will be able to view directions on how to navigate to events based on their location.


## Building Instruction
### For both release and debug build
- Put Firebase `google-services.json` file under the app level root directory
- Put Google Map SDK API Key in `local.properties` as `MAPS_API_KEY=${Value}`
### For debug build only
- Install [Firebase CLI](https://firebase.google.com/docs/cli#install_the_firebase_cli) and run `firebase login`

## Running Instruction
### For both release and debug build
- Update Google Play Services to the latest version
- If using an emulator, set the its location to USC Village
### For debug build only
- Run `./run-firebase-emulator.sh` before running the debug build. This will use our pre-defined user and event data to locally test the app. We have one dummy user with email `abc@xyz.com` and password `123456`
