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
    lib: {
      entry: path.resolve(__dirname, 'src/main.ts'),
      name: 'ExternalStorageViewer',
      formats: ['es', 'umd'],
      fileName: format => `external-storage-viewer.${format}.js`,
    },
    rollupOptions: {
      external: ['vue'],
      output: {
        globals: {
          vue: 'Vue',
        },
      },
    },
    outDir:
      '../../../../target/classes/META-INF/resources/vue/external-storage-viewer',
  },
  base: './',
};
