export function statusText(status: string): string {
  return status
    .toLowerCase()
    .split('_')
    .map((chunk) => chunk.charAt(0).toUpperCase() + chunk.slice(1))
    .join(' ')
}

export function formatDate(dateValue: string): string {
  const date = new Date(dateValue)
  if (Number.isNaN(date.getTime())) {
    return dateValue
  }
  return new Intl.DateTimeFormat('en-US', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

export function toBackendDateTime(datetimeLocal: string): string {
  if (!datetimeLocal) {
    return datetimeLocal
  }
  return datetimeLocal.length === 16 ? `${datetimeLocal}:00` : datetimeLocal
}
