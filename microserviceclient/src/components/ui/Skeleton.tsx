import { type HTMLAttributes } from 'react';

export interface SkeletonProps extends HTMLAttributes<HTMLDivElement> {
  /**
   * Width of the skeleton
   */
  width?: string | number;
  /**
   * Height of the skeleton
   */
  height?: string | number;
  /**
   * Shape of the skeleton
   */
  shape?: 'rectangle' | 'circle';
  /**
   * Animation variant
   */
  animation?: 'pulse' | 'wave' | 'none';
}

/**
 * Skeleton loader component for loading states.
 * Improves perceived performance with placeholder content.
 */
export const Skeleton = ({
  width,
  height,
  shape = 'rectangle',
  animation = 'pulse',
  className = '',
  style,
  ...props
}: SkeletonProps) => {
  const shapeStyles = {
    rectangle: 'rounded-lg',
    circle: 'rounded-full',
  };

  const animationStyles = {
    pulse: 'animate-pulse',
    wave: 'animate-shimmer',
    none: '',
  };

  const inlineStyles = {
    ...style,
    width: typeof width === 'number' ? `${width}px` : width,
    height: typeof height === 'number' ? `${height}px` : height,
  };

  return (
    <div
      className={`
        bg-gray-200
        ${shapeStyles[shape]}
        ${animationStyles[animation]}
        ${className}
      `.trim()}
      style={inlineStyles}
      aria-live="polite"
      aria-busy="true"
      {...props}
    />
  );
};

/**
 * Product Card Skeleton
 */
export const ProductCardSkeleton = () => {
  return (
    <div className="bg-white rounded-xl shadow-sm overflow-hidden border border-gray-100">
      <Skeleton height="100%" className="aspect-square" />
      <div className="p-4 space-y-2">
        <Skeleton height={16} className="w-3/4" />
        <Skeleton height={20} className="w-1/2" />
      </div>
    </div>
  );
};

/**
 * Text Lines Skeleton
 */
export const TextSkeleton = ({ lines = 3 }: { lines?: number }) => {
  return (
    <div className="space-y-2">
      {Array.from({ length: lines }).map((_, i) => (
        <Skeleton
          key={i}
          height={16}
          className={i === lines - 1 ? 'w-2/3' : 'w-full'}
        />
      ))}
    </div>
  );
};

