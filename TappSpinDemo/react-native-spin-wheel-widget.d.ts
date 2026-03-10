declare module 'react-native-spin-wheel-widget' {
  interface SpinWheelModule {
    show(): void;
    hide(): void;
    spin(onComplete?: (degrees: number) => void): void;
    getSpinCount(): Promise<number>;
    getCooldownTimeLeft(): Promise<number>;
    getCooldownFormatted(): Promise<string>;
  }

  const SpinWheel: SpinWheelModule;
  export default SpinWheel;
}