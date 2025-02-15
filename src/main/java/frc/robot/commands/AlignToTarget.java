// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import com.pathplanner.lib.PathConstraints;
import com.pathplanner.lib.PathPlanner;
import com.pathplanner.lib.PathPlannerTrajectory;
import com.pathplanner.lib.PathPoint;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory.State;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.Swerve;



public class AlignToTarget extends CommandBase {

  private Swerve swerve;
  private final Timer timer = new Timer();

  private Command trajCommand;
  private State idealState;
  private boolean isFinished = false;
  

  // Test Variables (Replace with the real ones when possible)
  private double testPValue = 0;
  private Translation2d testTagPos = new Translation2d(0, 0);
  private Rotation2d testTagRot = Rotation2d.fromDegrees(180);



  /** Creates a new AlignToTarget. 
   * 
   * This command will align the robot to an apriltag target. The robot will follow a path, and then utilize PID when it gets close. 
   * 
   * @param swerve The swerve drivebase subsystem
  */
  public AlignToTarget(Swerve swerve) {
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(swerve);

    this.timer.reset();
    this.timer.start();
    this.swerve = swerve;
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {

    PathPlannerTrajectory traj = PathPlanner.generatePath(
    new PathConstraints(4, 3), 
    new PathPoint(new Translation2d(swerve.getPose().getX(), swerve.getPose().getY()), swerve.getYaw(), swerve.getYaw()), // position, heading(direction to move in), orientation
    new PathPoint( testTagPos, swerve.getYaw(), testTagRot) // position, heading(direction to move in), orientation
);

int idealStateIdx = 0;
double idealStateDelta = 0;


for(int i = traj.getStates().size() / 2; i < traj.getStates().size(); i++) {

  Translation2d currentTranslation = new Translation2d(traj.getState(i).poseMeters.getX(), traj.getState(i).poseMeters.getY());
  double delta = Math.abs(traj.getState(i).velocityMetersPerSecond - currentTranslation.getDistance(testTagPos) * testPValue);

  if(delta <= idealStateDelta){
    idealStateIdx = i;
    idealStateDelta = delta;
  }
}

this.idealState = traj.getState(idealStateIdx);
this.trajCommand = swerve.followTrajectoryCommandCancelable(traj, this.idealState.timeSeconds);
this.trajCommand.schedule();
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    if(timer.get() >= idealState.timeSeconds){
      isFinished = swerve.PIDToPose(new Pose2d(testTagPos, testTagRot));
    }
  }


  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    trajCommand.cancel();
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return isFinished;
  }
}
