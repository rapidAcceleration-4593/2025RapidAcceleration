// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

/**
 * This class's methods are called automatically for each mode per TimedRobot documentation.
 * Update Main.java if the class or package name changes.
 */
public class Robot extends TimedRobot {
    private static Robot instance;
    private Command m_autonomousCommand;

    private RobotContainer m_robotContainer;

    private Timer disabledTimer;

    private Notifier dashboardNotifier;

    /** TargetReefBranch to be updated periodically through SmartDashboard. */
    public static int targetReefBranch;

    /** Match Time reflected by FMS. */
    private int lastMatchTime = -1;

    public Robot() {
        instance = this;
    }

    public static Robot getInstance() {
        return instance;
    }

    /** This function is run when the robot is first started up and should be used for any initialization code. */
    @Override
    public void robotInit() {
        // Instantiate our RobotContainer. This will perform all our button bindings, and put our autonomous chooser on the dashboard.
        m_robotContainer = new RobotContainer();

        // Create a timer to disable motor brake a few seconds after disable. This will let the robot stop
        // immediately when disabled, but then also let it be pushed more.
        disabledTimer = new Timer();

        if (isSimulation()) {
            DriverStation.silenceJoystickConnectionWarning(true);
        }

        // Create a Dashboard Notifier.
        dashboardNotifier = new Notifier(() -> {
            // Update SmartDashboard periodically, separate from Command Scheduler.
            int newTargetReefBranch = (int) SmartDashboard.getNumber("TargetReefBranch", 0);
            if (newTargetReefBranch != targetReefBranch) {
                targetReefBranch = newTargetReefBranch;
            }

            int currentMatchTime = (int) DriverStation.getMatchTime();
            if (currentMatchTime != lastMatchTime) {
                SmartDashboard.putNumber("MatchTime", currentMatchTime);
                lastMatchTime = currentMatchTime;
            }
            
            NetworkTableInstance.getDefault().flush();
        });

        // Start the Notifier to run every 0.1 seconds (200ms).
        dashboardNotifier.startPeriodic(0.2);
    }

    /**
     * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
     * that you want ran during disabled, autonomous, teleoperated, and test.
     * 
     * <p>This runs after the mode specific periodic functions, but before LiveWindow and
     * SmartDashboard integrated updating.
     */
    @Override
    public void robotPeriodic() {
        // Runs the Scheduler. This is responsible for polling buttons, adding newly-scheduled
        // commands, running already-scheduled commands, removing finished or interrupted commands,
        // and running subsystem periodic() methods. This must be called from the robot's periodic
        // block in order for anything in the Command-based framework to work.
        CommandScheduler.getInstance().run();
    }

    /** Called once when the robot enters Disabled mode. */
    @Override
    public void disabledInit() {
        m_robotContainer.setMotorBrake(true);
        disabledTimer.reset();
        disabledTimer.start();

        dashboardNotifier.stop();
    }

    /** Called periodically during Disabled mode. */
    @Override
    public void disabledPeriodic() {
        if (disabledTimer.hasElapsed(Constants.DrivebaseConstants.WHEEL_LOCK_TIME)) {
            m_robotContainer.setMotorBrake(false);
            disabledTimer.stop();
            disabledTimer.reset();
        }
    }

    /** Runs the autonomous command selected in {@link RobotContainer} class. */
    @Override
    public void autonomousInit() {
        m_robotContainer.setMotorBrake(true);
        m_autonomousCommand = m_robotContainer.getAutonomousCommand();

        if (m_autonomousCommand != null) {
            m_autonomousCommand.schedule();
        }
    }

    @Override
    public void teleopInit() {
        // This makes sure that the autonomous stops running when teleop starts running.
        // If you want the autonomous to continue until interrupted by another command, 
        // remove this line or comment it out
        if (m_autonomousCommand != null) {
            m_autonomousCommand.cancel();
        } else {
            CommandScheduler.getInstance().cancelAll();
        }
    }

    /** Called once when the robot enters Test mode. */
    @Override
    public void testInit() {
        CommandScheduler.getInstance().cancelAll();
    }
}
