/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{js,ts,tsx}',
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          950: '#071531',
          900: '#0A1F44', // navy
          800: '#EAF4FF', // page bg
          700: '#D6E7FF', // panels
          600: '#5A6C8C', // slate text
          500: '#E0B973', // gold accent
          100: '#FFF6E6', // beige panels
        }
      }
    }
  },
  plugins: []
}

