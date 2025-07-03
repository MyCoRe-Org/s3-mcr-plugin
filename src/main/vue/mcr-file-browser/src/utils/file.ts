import { FileInfo, FileFlag, FileCapability } from '@/types/file';

export const resolvePath = (file: FileInfo): string => {
  return file.parentPath !== '' ? `${file.parentPath}/${file.name}` : file.name;
};

export const getParentPath = (p: string): string => {
  return p.split('/').slice(0, -1).join('/');
};

export const isNavigable = (file: FileInfo): boolean => {
  return file.isDirectory || file.flags.includes(FileFlag.ARCHIVE);
};

export const isDownloadable = (file: FileInfo): boolean => {
  return (
    !file.isDirectory && file.capabilities.includes(FileCapability.DOWNLOAD)
  );
};

export const getFileSizeAsString = (file: FileInfo): string => {
  // Values used in MIR
  if (!file.size) {
    return '0';
  }
  const { radix, unit } = {
    radix: 1e3,
    unit: ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
  };
  let result = Math.abs(file.size);

  let loop = 0;
  // calculate

  while (result >= radix && loop < unit.length - 1) {
    result /= radix;
    loop += 1;
  }
  const formatted =
    loop === 0 ? Math.round(result).toString() : result.toFixed(1);
  return `${formatted} ${unit[loop]}`;
};
