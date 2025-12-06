# LoginProxy – Version 1.0

**LoginProxy** is a Velocity plugin that secures access before players reach the backend server.

## Features (v1.0)
- Blocks all commands except `/login` until the player is authenticated.
- Tracks authenticated sessions via **username + IP** to bypass login on future connections.
- Allows only one connection per session until the player logs in successfully.
- Lightweight and easy to deploy on Velocity proxies.
- Works in conjunction with a backend Paper/Spigot server where the actual password verification occurs.

## Usage
1. Place the plugin in your Velocity `plugins/` folder.
2. Restart the proxy.
3. Players must enter `/login <password>` to authenticate.
4. All other commands are blocked until successful login.

## License
Specify your license here (e.g., MIT, Apache 2.0, etc.)
