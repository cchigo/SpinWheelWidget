import { NativeModules } from 'react-native';

const { SpinWheelModule } = NativeModules;

if (!SpinWheelModule) {
  console.warn(
    '[react-native-spin-wheel-widget] SpinWheelModule is not linked. ' +
    'Make sure you ran `npm install` and rebuilt the Android project.'
  );
}

export default {
  show: () => {
    if (SpinWheelModule) SpinWheelModule.show();
  },

  hide: () => {
    if (SpinWheelModule) SpinWheelModule.hide();
  },

  spin: (onComplete) => {
    if (SpinWheelModule) SpinWheelModule.spin(onComplete);
  },

  getSpinCount: () => {
    if (SpinWheelModule) return SpinWheelModule.getSpinCount();
    return Promise.resolve(0);
  },

  getCooldownTimeLeft: () => {
    if (SpinWheelModule) return SpinWheelModule.getCooldownTimeLeft();
    return Promise.resolve(0);
  },

  getCooldownFormatted: () => {
    if (SpinWheelModule) return SpinWheelModule.getCooldownFormatted();
    return Promise.resolve('00:00');
  },
};