import { useState, useRef, type ReactNode } from 'react';

export interface TooltipProps {
  /**
   * Content to display in the tooltip
   */
  content: ReactNode;
  /**
   * Position of the tooltip
   */
  position?: 'top' | 'bottom' | 'left' | 'right';
  /**
   * Element that triggers the tooltip
   */
  children: ReactNode;
  /**
   * Delay before showing tooltip (ms)
   */
  delay?: number;
}

/**
 * Tooltip component that appears on hover.
 * Accessible with proper ARIA attributes.
 */
export const Tooltip = ({
  content,
  position = 'top',
  children,
  delay = 200,
}: TooltipProps) => {
  const [isVisible, setIsVisible] = useState(false);
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  const handleMouseEnter = () => {
    timeoutRef.current = setTimeout(() => {
      setIsVisible(true);
    }, delay);
  };

  const handleMouseLeave = () => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }
    setIsVisible(false);
  };

  const positionStyles = {
    top: 'bottom-full left-1/2 -translate-x-1/2 mb-2',
    bottom: 'top-full left-1/2 -translate-x-1/2 mt-2',
    left: 'right-full top-1/2 -translate-y-1/2 mr-2',
    right: 'left-full top-1/2 -translate-y-1/2 ml-2',
  };

  const arrowStyles = {
    top: 'top-full left-1/2 -translate-x-1/2 border-t-gray-900 border-x-transparent border-b-transparent',
    bottom: 'bottom-full left-1/2 -translate-x-1/2 border-b-gray-900 border-x-transparent border-t-transparent',
    left: 'left-full top-1/2 -translate-y-1/2 border-l-gray-900 border-y-transparent border-r-transparent',
    right: 'right-full top-1/2 -translate-y-1/2 border-r-gray-900 border-y-transparent border-l-transparent',
  };

  return (
    <div
      className="relative inline-block"
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      onFocus={handleMouseEnter}
      onBlur={handleMouseLeave}
    >
      {children}

      {isVisible && (
        <div
          role="tooltip"
          className={`
            absolute z-[1070] ${positionStyles[position]}
            px-3 py-2 text-sm text-white bg-gray-900 rounded-lg
            shadow-lg whitespace-nowrap
            animate-in fade-in duration-200
          `}
        >
          {content}
          {/* Arrow */}
          <div
            className={`
              absolute w-0 h-0
              border-4 ${arrowStyles[position]}
            `}
          />
        </div>
      )}
    </div>
  );
};

