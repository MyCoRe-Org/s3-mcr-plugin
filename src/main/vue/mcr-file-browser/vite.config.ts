import vue from '@vitejs/plugin-vue';
import eslint from 'vite-plugin-eslint';
import path from 'path';
import { defineConfig } from 'vite';

export default defineConfig(({ command }) => ({
  plugins: [vue(), eslint()],
  resolve: {
    alias: [
      { find: '@', replacement: `${path.resolve(__dirname, './src')}/` },
      // use the Bootstrap bundle of the host page instead of bundling it
      ...(command === 'build'
        ? [
            {
              find: /^bootstrap$/,
              replacement: path.resolve(__dirname, 'src/shims/bootstrap.ts'),
            },
          ]
        : []),
    ],
  },
  build: {
    lib: {
      entry: path.resolve(__dirname, 'src/main.ts'),
      name: 'ExternalStorageViewer',
      formats: ['es', 'umd'],
      fileName: format => `external-storage-viewer.${format}.js`,
      cssFileName: 'external-storage-viewer',
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
}));
