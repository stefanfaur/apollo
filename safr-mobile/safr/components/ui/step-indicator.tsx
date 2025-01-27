import React from 'react';
import { View, Text, StyleSheet, Animated } from 'react-native';
import { Colors } from '@/constants/colors';

interface StepIndicatorProps {
  steps: string[];
  currentStep: number;
  progress?: Animated.Value;
}

const StepIndicator: React.FC<StepIndicatorProps> = ({ 
  steps, 
  currentStep,
  progress 
}) => {
  const progressWidth = progress?.interpolate({
    inputRange: [0, 1],
    outputRange: ['0%', '100%'],
  }) || (`${(currentStep / (steps.length - 1)) * 100}%` as const);

  return (
    <View style={styles.outerContainer}>
      <View style={styles.container}>
        <View style={styles.stepsContainer}>
          {steps.map((step, index) => (
            <View 
              key={step} 
              style={[
                styles.stepContainer,
                index === steps.length - 1 && { marginRight: 0 }
              ]}
            >
              <View style={[
                styles.circle,
                index <= currentStep && styles.activeCircle
              ]}>
                <Text style={[
                  styles.stepNumber,
                  index <= currentStep && styles.activeStepNumber
                ]}>
                  {index + 1}
                </Text>
              </View>
              <Text style={[
                styles.stepText,
                index <= currentStep && styles.activeStepText
              ]}>
                {step}
              </Text>
            </View>
          ))}
        </View>
        
        <View style={styles.progressBarContainer}>
          <Animated.View 
            style={[
              styles.progressBar,
              { width: progressWidth }
            ]} 
          />
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  outerContainer: {
    paddingHorizontal: 16,
    marginTop: 8,
    marginBottom: 24,
  },
  container: {
    paddingHorizontal: 20,
    paddingVertical: 20,
    backgroundColor: Colors.dark.background + '40',
    borderRadius: 16,
  },
  stepsContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'flex-start',
    marginBottom: 20,
  },
  stepContainer: {
    alignItems: 'center',
    marginRight: 40,
    width: 100,
  },
  circle: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: Colors.dark.buttonBackground,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 8,
    borderWidth: 2,
    borderColor: Colors.dark.background,
    shadowColor: "#000",
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.2,
    shadowRadius: 3.84,
    elevation: 3,
  },
  activeCircle: {
    backgroundColor: Colors.dark.primary,
    borderColor: Colors.dark.primary + '40',
  },
  stepNumber: {
    color: Colors.dark.text,
    fontSize: 14,
    fontWeight: 'bold',
  },
  activeStepNumber: {
    color: Colors.dark.background,
    fontSize: 16,
  },
  stepText: {
    color: Colors.dark.text,
    fontSize: 13,
    textAlign: 'center',
    opacity: 0.7,
  },
  activeStepText: {
    opacity: 1,
    fontWeight: 'bold',
    color: Colors.dark.primary,
  },
  progressBarContainer: {
    height: 3,
    backgroundColor: Colors.dark.buttonBackground,
    borderRadius: 1.5,
    overflow: 'hidden',
  },
  progressBar: {
    position: 'absolute',
    left: 0,
    top: 0,
    bottom: 0,
    backgroundColor: Colors.dark.primary,
    borderRadius: 1.5,
    shadowColor: Colors.dark.primary,
    shadowOffset: {
      width: 0,
      height: 0,
    },
    shadowOpacity: 0.5,
    shadowRadius: 4,
    elevation: 4,
  },
});

export default StepIndicator;
