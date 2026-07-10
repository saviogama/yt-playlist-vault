package com.savio.ytplaylistvault.snapshot;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SnapshotItemRepository extends JpaRepository<SnapshotItem, UUID> {
  List<SnapshotItem> findBySnapshotOrderByPositionAsc(Snapshot snapshot);

  List<SnapshotItem> findBySnapshot(Snapshot snapshot);
}
