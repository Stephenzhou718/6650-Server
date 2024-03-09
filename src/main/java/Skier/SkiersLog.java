package Skier;

public class SkiersLog {

  private Integer resortID;
  private Integer seasonID;
  private Integer dayID;
  private Integer skierID;
  private Long time;
  private Long liftID;

  public SkiersLog(Integer resortID, Integer seasonID, Integer dayID, Integer skierID, Long time,
      Long liftID) {
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.skierID = skierID;
    this.time = time;
    this.liftID = liftID;
  }

  public Long getTime() {
    return time;
  }

  public void setTime(Long time) {
    this.time = time;
  }

  public Long getLiftID() {
    return liftID;
  }

  public void setLiftID(Long liftID) {
    this.liftID = liftID;
  }

  public Integer getResortID() {
    return resortID;
  }

  public void setResortID(Integer resortID) {
    this.resortID = resortID;
  }

  public Integer getSeasonID() {
    return seasonID;
  }

  public void setSeasonID(Integer seasonID) {
    this.seasonID = seasonID;
  }

  public Integer getDayID() {
    return dayID;
  }

  public void setDayID(Integer dayID) {
    this.dayID = dayID;
  }

  public Integer getSkierID() {
    return skierID;
  }

  public void setSkierID(Integer skierID) {
    this.skierID = skierID;
  }
}
