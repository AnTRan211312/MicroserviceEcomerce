import { type HTMLAttributes } from 'react';

export interface AvatarProps extends HTMLAttributes<HTMLDivElement> {
  /**
   * Image source URL
   */
  src?: string;
  /**
   * Alt text for the image
   */
  alt?: string;
  /**
   * Initials to display when no image is provided
   */
  initials?: string;
  /**
   * Size of the avatar
   */
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  /**
   * Shape of the avatar
   */
  shape?: 'circle' | 'square';
  /**
   * Status indicator
   */
  status?: 'online' | 'offline' | 'away' | 'busy';
}

/**
 * Avatar component for displaying user profile images.
 * Supports fallback to initials and status indicators.
 */
export const Avatar = ({
  src,
  alt = 'Avatar',
  initials,
  size = 'md',
  shape = 'circle',
  status,
  className = '',
  ...props
}: AvatarProps) => {
  const sizeStyles = {
    xs: 'w-6 h-6 text-xs',
    sm: 'w-8 h-8 text-sm',
    md: 'w-10 h-10 text-base',
    lg: 'w-12 h-12 text-lg',
    xl: 'w-16 h-16 text-xl',
  };

  const shapeStyles = {
    circle: 'rounded-full',
    square: 'rounded-lg',
  };

  const statusColors = {
    online: 'bg-green-500',
    offline: 'bg-gray-400',
    away: 'bg-yellow-500',
    busy: 'bg-red-500',
  };

  const statusSize = {
    xs: 'w-1.5 h-1.5',
    sm: 'w-2 h-2',
    md: 'w-2.5 h-2.5',
    lg: 'w-3 h-3',
    xl: 'w-4 h-4',
  };

  return (
    <div className={`relative inline-block ${className}`} {...props}>
      <div
        className={`
          ${sizeStyles[size]}
          ${shapeStyles[shape]}
          flex items-center justify-center
          bg-gray-200 text-gray-600 font-semibold
          overflow-hidden
        `}
      >
        {src ? (
          <img
            src={src}
            alt={alt}
            className="w-full h-full object-cover"
            loading="lazy"
          />
        ) : initials ? (
          <span>{initials.slice(0, 2).toUpperCase()}</span>
        ) : (
          <svg
            className="w-2/3 h-2/3 text-gray-400"
            fill="currentColor"
            viewBox="0 0 20 20"
          >
            <path
              fillRule="evenodd"
              d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z"
              clipRule="evenodd"
            />
          </svg>
        )}
      </div>

      {/* Status Indicator */}
      {status && (
        <span
          className={`
            absolute bottom-0 right-0
            ${statusSize[size]}
            ${statusColors[status]}
            ${shape === 'circle' ? 'rounded-full' : 'rounded-sm'}
            border-2 border-white
          `}
          aria-label={`Status: ${status}`}
        />
      )}
    </div>
  );
};

