import vue from '@vitejs/plugin-vue';
import eslint from 'vite-plugin-eslint';
import path from 'path';

export default {
  plugins: [vue(), eslint()],
  resolve: {
    alias: {
      '@': `${path.resolve(__dirname, './src')}/`,
    },
  },
  build: {
    outDir: '../../../../target/classes/META-INF/resources/access-key-manager',
  },
  base: './',
};
