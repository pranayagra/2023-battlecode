package lessadcarriers.knowledge;

import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.WellInfo;

public class WellData {
  public MapLocation loc;

  public ResourceType type;

  public boolean isUpgraded;
  // # of spots available for carriers to mine from. This should not include: walls, bad currents, other wells (WIP).
  public int capacity;
  public boolean dirty;

  public WellData(WellInfo wellInfo, int capacity) {
    this.loc = wellInfo.getMapLocation();
    this.type = wellInfo.getResourceType();
    this.isUpgraded = wellInfo.isUpgraded();
    this.capacity = capacity;
  }

  public WellData(MapLocation wellLocation, ResourceType resourceType, boolean isUpgraded, int capacity) {
    this.loc = wellLocation;
    this.type = resourceType;
    this.isUpgraded = isUpgraded;
    this.capacity = capacity;
  }

  /**
   * Merges two well info assuming that the locations are the same.
   * Does not check this!
   * Mutates the current instance of welldata
   * @param otherWellData a pointer to this object
   */
  public WellData merge(WellData otherWellData) {
    isUpgraded |= otherWellData.isUpgraded;
    type = otherWellData.type == ResourceType.ELIXIR ? ResourceType.ELIXIR : type;
    capacity = Math.max(capacity, otherWellData.capacity);
    return this;
  }
}
