# PME123 Windspotter

...

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Features

....

## Technology Stack

- **Frontend**: Scala.js 1.17.0 with Scala 3.6.2
- **UI Framework**: Laminar 17.2.0 for reactive UI
- **UI Components**: UI5 Web Components 2.1.0
- **Build Tools**: SBT and Vite 6.0.0
- **Development**: Fast development with Vite hot reload

## Quick Start

### Prerequisites

- Java 11 or higher
- Node.js 18 or higher
- SBT 1.9.6

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/pme123/pme123-windspotter.git
   cd pme123-windspotter
   ```

2. Install JavaScript dependencies:
   ```bash
   npm install
   ```

### Development

1. Start the Scala.js compiler in watch mode:
   ```bash
   sbt ~fastLinkJS
   ```

2. In a separate terminal, start the development server:
   ```bash
   npm run dev
   ```

3. Open your browser and navigate to:
   ```
   http://localhost:5173
   ```

The application will automatically reload when you make changes to the Scala code.

### Production Build

1. Create an optimized production build:
   ```bash
   sbt fullLinkJS
   npm run build
   ```

2. Preview the production build:
   ```bash
   npm run preview
   ```

3. Deploy to GitHub Pages (automated):
   ```bash
   scala helper.scala
   ```
   
   This script will:
   - Build the optimized Scala.js bundle
   - Create the production build
   - Adjust asset paths for GitHub Pages
   - Copy assets to the docs directory
   - Commit and push changes to deploy

## Project Structure

```
pme123-windspotter/
├── build.sbt                 # SBT build configuration
├── package.json              # Node.js dependencies and scripts
├── vite.config.js           # Vite configuration
├── index.html               # HTML template
├── project/
│   ├── build.properties     # SBT version
│   └── plugins.sbt          # SBT plugins
├── src/main/
│   ├── scala/pme123/windspotter/
│   │   ├── Main.scala       # Application entry point
│   │   └── HelloWorldView.scala  # Demo component
│   └── resources/
│       └── styles.css       # Application styles
└── README.md
```

## Development Workflow

1. **Code Changes**: Edit Scala files in `src/main/scala/`
2. **Automatic Compilation**: SBT watch mode compiles changes automatically
3. **Hot Reload**: Vite detects compiled JavaScript changes and reloads the browser
4. **Styling**: Modify `src/main/resources/styles.css` for UI customization

## Available Scripts

- `npm run dev` - Start development server with hot reload
- `npm run build` - Create production build
- `npm run preview` - Preview production build locally
- `sbt ~fastLinkJS` - Compile Scala.js in watch mode (development)
- `sbt fullLinkJS` - Create optimized Scala.js build (production)

## Key Dependencies

### Scala Dependencies
- **Laminar 17.2.0**: Reactive UI library with advanced features
- **UI5 Web Components 2.1.0**: Enterprise-grade UI component library

### JavaScript Dependencies
- **Vite 6.0.0**: Fast build tool and development server
- **UI5 Web Components**: Complete UI component ecosystem

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Built with [Laminar](https://laminar.dev/) - Reactive UI library for Scala.js
- UI components powered by [UI5 Web Components](https://sap.github.io/ui5-webcomponents/)
- Fast development experience with [Vite](https://vitejs.dev/)

## Support

For questions, issues, or contributions, please visit our [GitHub repository](https://github.com/pme123/pme123-windspotter).
