# AI Audio Plugin

AI-powered voice processing and interaction plugin for Fluxcord, bringing advanced speech recognition, text-to-speech, and audio analysis capabilities to Discord bots.

## Features

### 🎤 Speech Recognition

- **Real-time Transcription**: Live voice-to-text conversion
- **Multi-language Support**: Support for dozens of languages
- **High Accuracy**: Advanced AI models for precise transcription
- **Custom Vocabulary**: Add domain-specific terms and names
- **Speaker Identification**: Distinguish between different speakers

### 🗣️ Text-to-Speech

- **Natural Voices**: High-quality, human-like speech synthesis
- **Multiple Languages**: Support for various languages and accents
- **Voice Selection**: Choose from different voice personalities
- **SSML Support**: Advanced speech markup for natural delivery
- **Customizable Speed**: Adjust speech rate and pitch

### 📊 Audio Analysis

- **Audio Quality Assessment**: Analyze audio clarity and quality
- **Noise Detection**: Identify and filter background noise
- **Emotion Recognition**: Detect emotional tone in speech
- **Content Analysis**: Identify inappropriate content
- **Pattern Recognition**: Analyze speaking patterns and habits

### 🎯 Voice Commands

- **Natural Language Processing**: Understand conversational commands
- **Context Awareness**: Maintain conversation context
- **Custom Wake Words**: Configure personalized activation phrases
- **Command Chaining**: Execute multiple commands in sequence
- **Intent Recognition**: Understand user intentions beyond literal commands

### 🤖 AI Integration

- **OpenAI Integration**: Leverage GPT models for conversation
- **Google Cloud AI**: Advanced speech and language processing
- **Custom Models**: Support for self-hosted AI models
- **Multi-provider Support**: Failover between different AI services
- **Cost Optimization**: Smart usage management and caching

## Commands

| Command               | Description                 | Permission              |
|-----------------------|-----------------------------|-------------------------|
| `/transcribe`         | Start voice transcription   | `aiaudio.transcribe`    |
| `/speak <text>`       | Convert text to speech      | `aiaudio.tts`           |
| `/analyze`            | Analyze current audio       | `aiaudio.analyze`       |
| `/voicecommands`      | Toggle voice command mode   | `aiaudio.voicecommands` |
| `/setvoice <voice>`   | Change TTS voice            | `aiaudio.tts`           |
| `/setlanguage <lang>` | Set recognition language    | `aiaudio.transcribe`    |
| `/confidence`         | Show recognition confidence | `aiaudio.analyze`       |
| `/noise-filter`       | Toggle noise filtering      | `aiaudio.admin`         |

## Installation

1. **Prerequisites**
    - OpenAI API key (for GPT integration)
    - Google Cloud credentials (for advanced features)
    - Sufficient server resources for AI processing

2. **Build the plugin**
   ```bash
   cd plugins/ai-audio-plugin
   mvn clean package
   ```

3. **Install the plugin**
   ```bash
   cp target/ai-audio-plugin-*.jar ../../plugins/
   ```

4. **Configure API keys**
   ```yaml
   ai:
     openai_api_key: "your-openai-api-key"
     google_credentials_path: "/path/to/google-credentials.json"
   ```

5. **Restart the bot** to load the AI audio plugin

## Configuration

```yaml
ai:
  # API Configuration
  openai_api_key: ""
  google_credentials_path: ""
  
  # Speech Recognition
  transcription_language: "en-US"
  confidence_threshold: 0.8
  enable_speaker_identification: true
  
  # Text-to-Speech
  tts_voice: "en-US-Standard-A"
  speech_rate: 1.0
  pitch: 0.0
  
  # Voice Commands
  enable_voice_commands: true
  wake_word: "hey bot"
  command_timeout: 5000  # milliseconds
  
  # Audio Processing
  enable_noise_reduction: true
  auto_gain_control: true
  echo_cancellation: true
  
  # Cost Management
  max_monthly_requests: 10000
  cache_responses: true
  cache_duration: 3600  # seconds
```

## Permissions

- `aiaudio.transcribe` - Use voice transcription features
- `aiaudio.tts` - Access text-to-speech functionality
- `aiaudio.analyze` - Perform audio analysis
- `aiaudio.voicecommands` - Use voice command features
- `aiaudio.admin` - Administrative AI audio controls

## AI Service Setup

### OpenAI Configuration

1. Get an API key from [OpenAI Platform](https://platform.openai.com/)
2. Add the key to your configuration
3. Configure usage limits to control costs

### Google Cloud Setup

1. Create a Google Cloud project
2. Enable the Speech-to-Text and Text-to-Speech APIs
3. Download service account credentials
4. Configure the credentials path

### Custom Models

Support for self-hosted models is planned for future releases.

## Privacy & Security

- **Data Handling**: Audio is processed in real-time and not stored by default
- **Encryption**: All API communications use secure HTTPS
- **Opt-out**: Users can disable AI processing for their voice
- **Compliance**: GDPR and privacy-friendly configurations available

## Development Status

🚧 **Under Development** - This plugin is currently being implemented.

Core features planned:

- [x] Basic plugin structure and commands
- [ ] Speech recognition service integration
- [ ] Text-to-speech implementation
- [ ] Audio analysis capabilities
- [ ] Voice command processing
- [ ] AI service integrations (OpenAI, Google Cloud)
- [ ] Privacy controls and data management
- [ ] Performance optimization and caching

## Performance Considerations

- **CPU Usage**: AI processing can be CPU-intensive
- **Memory**: Models may require significant RAM
- **Latency**: Real-time processing adds some delay
- **Bandwidth**: API calls require stable internet connection
- **Costs**: AI services have usage-based pricing

## Contributing

This plugin is part of the Fluxcord ecosystem. Contributions welcome!

1. Fork the repository
2. Create a feature branch for AI audio enhancements
3. Implement your changes with proper testing
4. Ensure privacy and security best practices
5. Submit a pull request with detailed documentation

## License

Part of Fluxcord - Licensed under MIT License