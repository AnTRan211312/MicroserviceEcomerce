import type { SessionMetaRequest } from '../types/auth';

/**
 * Get session metadata from user agent - automatically parsed
 */
export function getSessionMeta(): SessionMetaRequest {
  // Simple implementation without external dependencies
  const userAgent = navigator.userAgent;
  
  // Detect device type
  let deviceType = 'desktop';
  if (/Mobile|Android|iPhone|iPad/.test(userAgent)) {
    deviceType = /iPad/.test(userAgent) ? 'tablet' : 'mobile';
  }
  
  // Simple device name detection
  let deviceName = 'Unknown Device';
  if (/Windows/.test(userAgent)) {
    deviceName = 'Windows Device';
  } else if (/Mac/.test(userAgent)) {
    deviceName = 'Mac Device';
  } else if (/Linux/.test(userAgent)) {
    deviceName = 'Linux Device';
  } else if (/Android/.test(userAgent)) {
    deviceName = 'Android Device';
  } else if (/iPhone|iPad/.test(userAgent)) {
    deviceName = 'iOS Device';
  }

  return {
    deviceName,
    deviceType,
    userAgent,
  };
}
