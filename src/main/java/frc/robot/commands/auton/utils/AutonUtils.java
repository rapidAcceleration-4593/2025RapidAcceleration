package frc.robot.commands.auton.utils;

import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.path.PathPlannerPath;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.FieldConstants;
import frc.robot.subsystems.SwerveSubsystem;

public class AutonUtils {

    /** SwerveSubsystem Object. */
    private SwerveSubsystem drivebase;

    /** Constructor for AutonUtils. */
    public AutonUtils(SwerveSubsystem drivebase) {
        this.drivebase = drivebase;
    }

    /**
     * Command to reset the robot's odometry to initial pose, adjusted for the current
     * alliance color by flipping it if necessary.
     * @param choreoPath The PathPlannerPath containing the trajectory to use for
     *                   resetting the robot's odometry.
     * @return           A command that, when run, resets the robot's odometry to the
     *                   initial pose of given path.
     */
    public Command resetOdometry(PathPlannerPath path) {
        return drivebase.runOnce(
            () -> {
                RobotConfig config = getRobotConfig();

                Pose2d pose = path
                    .generateTrajectory(new ChassisSpeeds(), new Rotation2d(Math.PI), config)
                    .getInitialPose();

                if (drivebase.isRedAlliance()) {
                    pose = flipFieldPose(pose);
                }
    
                drivebase.resetOdometry(pose);
        });
    }

    /**
     * Load the PathPlanner trajectory file to path.
     * @param pathName Name of the path.
     * @return PathPlanner Path.
     */
    public PathPlannerPath loadPath(String pathName) {
        try {
            return PathPlannerPath.fromPathFile(pathName);
        } catch (Exception e) {
            // Handle exception, as needed.
            e.printStackTrace();
            throw new RuntimeException("Failed to load path: " + pathName, e);
        }
    }

    /**
     * Flip a field position to the other side of the field, maintaining a blue alliance origin.
     * @param position The position to flip.
     * @return The flipped position.
     */
    private Translation2d flipFieldPosition(Translation2d position) {
        return new Translation2d(FieldConstants.FIELD_LENGTH - position.getX(), position.getY());
    }

    /**
     * Flip a field rotation to the other side of the field, maintaining a blue alliance origin.
     * @param rotation The rotation to flip.
     * @return The flipped rotation.
     */
    private Rotation2d flipFieldRotation(Rotation2d rotation) {
        return new Rotation2d(Math.PI).minus(rotation);
    }
    
    /**
     * Flip a field pose to the other side of the field, maintaining a blue alliance origin.
     * @param pose The pose to flip.
     * @return The flipped pose.
     */
    public Pose2d flipFieldPose(Pose2d pose) {
        return new Pose2d(flipFieldPosition(pose.getTranslation()), flipFieldRotation(pose.getRotation()));
    }

    /**
     * Retrieves the robot configuration from Deploy Settings.
     * @return The RobotConfig instance generated by PathPlanner.
     * @throws RuntimeException If failed to retrieve configuration.
     */
    public RobotConfig getRobotConfig() {
        try {
            return RobotConfig.fromGUISettings();
        } catch (Exception e) {
            System.err.println("Failed to retrieve RobotConfig from Deploy Settings.");
            e.printStackTrace();
            throw new RuntimeException("Error retrieving RobotConfig.", e);
        }
    }

    /** Pose2d for Coral Station on bottom of blue alliance. */
    public final Pose2d[] BLUE_BOTTOM_CHUTE = {
        new Pose2d(0.5781, 1.3135, Rotation2d.fromDegrees(-126)),
        new Pose2d(1.0714, 0.9553, Rotation2d.fromDegrees(-126)),
        new Pose2d(1.5646, 0.5971, Rotation2d.fromDegrees(-126))
    };

    /** Pose2d for Coral Station on top of blue alliance. */
    public final Pose2d[] BLUE_TOP_CHUTE = {
        new Pose2d(0.5781, 6.7383, Rotation2d.fromDegrees(126)),
        new Pose2d(1.0714, 7.0965, Rotation2d.fromDegrees(126)),
        new Pose2d(1.5646, 7.4547, Rotation2d.fromDegrees(126))
    };

    /** Pose2d for Coral Station on bottom of red alliance. */
    public final Pose2d[] RED_BOTTOM_CHUTE = flipFieldPoses(BLUE_BOTTOM_CHUTE);

    /** Pose2d for Coral Station on top of red alliance. */
    public final Pose2d[] RED_TOP_CHUTE = flipFieldPoses(BLUE_TOP_CHUTE);

    /** Flip Pose2d locations on field for Coral Station on red alliance. */
    private Pose2d[] flipFieldPoses(Pose2d[] bluePoses) {
        Pose2d[] redPoses = new Pose2d[bluePoses.length];
        for (int i = 0; i < bluePoses.length; i++) {
            redPoses[i] = flipFieldPose(bluePoses[i]);
        }
        return redPoses;
    }
}