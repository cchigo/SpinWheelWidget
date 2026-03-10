import React from 'react';
import { View, Button, Alert, StyleSheet } from 'react-native';
import SpinWheel from 'react-native-spin-wheel-widget';

export default function SpinWheelDemo() {

  const showSpinWheel = async () => {
    try {
      await SpinWheel.show();
    } catch (err) {
      Alert.alert('Error', `SpinWheel.show() failed: ${err}`);
    }
  };

  const hideSpinWheel = async () => {
    try {
      await SpinWheel.hide();
    } catch (err) {
      Alert.alert('Error', `SpinWheel.hide() failed: ${err}`);
    }
  };

  const spinWheel = () => {
    try {
      SpinWheel.spin((degrees) => {
        Alert.alert('Spin Complete', `Landed on: ${degrees}°`);
      });
    } catch (err) {
      Alert.alert('Error', `SpinWheel.spin() failed: ${err}`);
    }
  };

  const getSpinCount = async () => {
    try {
      const count = await SpinWheel.getSpinCount();
      Alert.alert('Spin Count', `Total spins: ${count}`);
    } catch (err) {
      Alert.alert('Error', `SpinWheel.getSpinCount() failed: ${err}`);
    }
  };

  const getCooldown = async () => {
    try {
      const timeLeft = await SpinWheel.getCooldownTimeLeft();
      const formatted = await SpinWheel.getCooldownFormatted();
      Alert.alert('Cooldown', `Milliseconds left: ${timeLeft}\nFormatted: ${formatted}`);
    } catch (err) {
      Alert.alert('Error', `SpinWheel.getCooldown() failed: ${err}`);
    }
  };

  return (
    <View style={styles.container}>
      <Button title="Show Spin Wheel" onPress={showSpinWheel} />
      <Button title="Spin Wheel" onPress={spinWheel} />
      <Button title="Get Spin Count" onPress={getSpinCount} />
      <Button title="Get Cooldown" onPress={getCooldown} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    gap: 12,
    padding: 20,
  },
});