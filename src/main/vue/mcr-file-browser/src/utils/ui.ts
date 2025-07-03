import { Crumb } from '@/types/ui';

export const buildBreadcrumbs = (title: string, path: string): Crumb[] => {
  if (path.length === 0) {
    return [];
  }
  const crumbLabels = path.split('/');
  const crumbs: Crumb[] = [{ label: title, id: '' }];
  const until = [];
  for (let i = 0; i < crumbLabels.length; i++) {
    until.push(crumbLabels[i]);
    crumbs.push({
      id: `${until.join('/')}`,
      label: crumbLabels[i],
    });
  }
  return crumbs;
};

export const getDateString = (
  value: Date | string | number,
  locale: string
): string => {
  try {
    const date = new Date(value);
    if (locale.startsWith('de')) {
      return new Intl.DateTimeFormat('de-DE').format(date);
    } else {
      return date.toISOString().split('T')[0];
    }
  } catch {
    return '';
  }
};
