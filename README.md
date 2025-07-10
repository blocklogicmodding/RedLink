# RedLink - Wireless Redstone Control

![RedLink Logo](https://deonjonker.com/blm/redlink/rl_banner.png)

A wireless redstone control mod that transforms simple remote activation into a multi-channel network system.

## Features

### Multi-Channel System
- **8 Configurable Channels** - Each with custom names and pulse frequencies
- **Color-Coded Organization** - Visual channel identification with unique colors
- **Channel Capacity** - Configurable limits per channel with real-time monitoring

### Wireless Components
- **Transceiver Hub** - Central command center for network management
- **Transceivers** - Wireless redstone transmitters/receivers that can be linked to any channel
- **Redstone Remote** - Handheld device for channel control and activation

### Operating Modes
- **Toggle Mode** - Transceivers stay on/off until manually changed
- **Pulse Mode** - Automatic pulsing with configurable frequencies (like a wireless redstone clock)

### Multiple Control Methods
- **Mouse Controls** - Right-click to activate, Shift+Right-click to cycle channels
- **Keybind Support** - `[` and `]` keys for quick channel switching (default, can be changed)
- **GUI Management** - Interface for channel configuration

### Real-Time Monitoring
- **HUD Overlay** - Live channel status when holding the remote
- **Transceiver Counting** - Real-time tracking of devices per channel
- **Visual Feedback** - Particle effects and wireframe rendering
- **Capacity Warnings** - Clear indicators when channels reach limits

### Advanced Configuration
- **Custom Channel Names** - Personalize your network organization
- **Pulse Frequency Control** - Fine-tune timing from 1 to 200 ticks
- **Range Settings** - Configurable wireless range (4-128 blocks)
- **Hub Naming** - Give your control centers custom identities

## Getting Started

1. **Craft a Transceiver Hub** - Your network's command center
2. **Create Transceivers** - Place them where you need wireless redstone signals
3. **Build a Redstone Remote** - Your portable control device
4. **Link Everything** - Bind the remote to a hub, then link transceivers to channels

![Remote Recipe](https://deonjonker.com/blm/redlink/recipes/recipes_remote.png)

![Hub Recipe](https://deonjonker.com/blm/redlink/recipes/recipes_hub.png)

![Transceiver Recipe](https://deonjonker.com/blm/redlink/recipes/recipes_transceivers.png)

## Usage

### Setting Up Your Network
- Place a Transceiver Hub and right-click to open the configuration GUI
- Customize hub name, channel names, and pulse frequencies
- Craft and bind a Redstone Remote by Shift+Right-clicking the hub

### Linking Transceivers
- Hold your bound remote and right-click any transceiver to link it to the current channel
- Shift+Right-click transceivers to switch between Toggle and Pulse modes
- Right-click (empty hand) to check transceiver status and channel info

### Channel Control
- **Right-click** with remote: Activate/deactivate all transceivers on current channel
- **Shift+Right-click** with remote: Cycle to next channel
- **`[` / `]` keys**: Quick previous/next channel switching
- **HUD Display**: View all channel statuses in real-time

## Configuration

RedLink includes configuration options:
- **Remote range and overlay settings**
- **Transceiver limits and pulse timing**
- **Hub and Channel name length restrictions**
- **HUD appearance and positioning**

All settings are adjustable via the mod's configuration file.

## Technical Details

- **Range**: Configurable wireless range (default: 16 blocks)
- **Channels**: 8 simultaneous channels per hub
- **Capacity**: Configurable transceivers per channel (default: 16)
- **Pulse Timing**: 1-200 ticks (20 ticks = 1 second)

## License

All rights reserved. This mod is protected by copyright and may not be redistributed or modified without explicit permission.

**Permitted Uses:**

-   Inclusion in modpacks (public or private)
-   Content creation (videos, streams, reviews, etc.)

----------

[**Issue Tracker**](https://github.com/blocklogicmodding/RedLink/issues) | [**BLM Discord**](https://discord.gg/YtdA3AMqsX)