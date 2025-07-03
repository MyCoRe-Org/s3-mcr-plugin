import { DerivateInfo } from '@/types/storage/core';

export const resolveTitle = (info: DerivateInfo): string => {
  if (info.titles.length > 0) {
    return info.titles[0].text;
  }
  return info.id;
};
