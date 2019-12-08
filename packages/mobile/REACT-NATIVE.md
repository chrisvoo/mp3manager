# Notes about React Native

## Requirements

- Node 10+
- [Expo CLI](https://expo.io/)
- Expo app fro Android

## Installation

```bash
npm install -g expo-cli
expo init AwesomeProject
cd AwesomeProject
npm start # you can also use: expo start
```

I got some errors during the installation of Expo with sudo. See this [issue](https://github.com/expo/expo-cli/issues/412).

Install the Expo client app on your iOS or Android phone and connect to the same wireless network as your computer. On Android, use the Expo app to scan the QR code from your terminal to open your project.

## Features and caveats

**It's not possible to include custom native modules** beyond the React Native APIs and components that are available in the Expo client app.

If you know that you'll eventually need to include your own native code, Expo is still a good way to get started. In that case you'll just need to ["eject"](https://docs.expo.io/versions/latest/expokit/eject) eventually to create your own native builds.

[Cookie based authentication is currently unstable](https://github.com/facebook/react-native/issues/23185)

### Style

React Native is like React, but it uses **native components instead of web components** as building blocks.

A component can only expand to fill available space if its parent has dimensions greater than 0. If a parent does not have either a fixed width and height or flex, the parent will have dimensions of 0 and the flex children will not be visible.

### Networking

React Native provides the Fetch API for your networking needs. Fetch will seem familiar if you have used XMLHttpRequest or other networking APIs before. You may refer to MDN's guide on [Using Fetch](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch) for additional information.

The [XMLHttpRequest API](https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest) is built in to React Native. This means that you can use third party libraries such as frisbee or [axios](https://github.com/mzabriskie/axios) that depend on it, or you can use the XMLHttpRequest API directly if you prefer.

React Native also supports WebSockets, a protocol which provides full-duplex communication channels over a single TCP connection.

## References

- [Expo docs](https://docs.expo.io/versions/latest/)
- [React Native Web Player](http://dabbott.github.io/react-native-web-player/): Build and run React Native apps in your browser!
- [Awesome React Native](http://www.awesome-react-native.com/)
- [Dive into React Native performance](https://code.fb.com/android/dive-into-react-native-performance/)
- [React Native Apps showcase](https://github.com/ReactNativeNews/React-Native-Apps)
- [Building the F8 App](https://makeitopen.com/)