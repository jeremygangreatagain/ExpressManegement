/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        orange: {
          500: '#FF6700', // 主题色
          600: '#E05A00', // 深一点的主题色，用于hover状态
        },
      },
    },
  },
  plugins: [],
}