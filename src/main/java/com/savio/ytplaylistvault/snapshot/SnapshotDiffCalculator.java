package com.savio.ytplaylistvault.snapshot;

import com.savio.ytplaylistvault.snapshot.dto.SnapshotDiffItemResponse;
import com.savio.ytplaylistvault.snapshot.dto.SnapshotDiffResponse;
import com.savio.ytplaylistvault.snapshot.dto.SnapshotMovedItemResponse;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SnapshotDiffCalculator {

  public SnapshotDiffResponse calculate(
      Snapshot currentSnapshot,
      Snapshot previousSnapshot,
      List<SnapshotItem> currentItems,
      List<SnapshotItem> previousItems) {
    Map<String, SnapshotItem> currentItemsByProviderItemId = indexByProviderItemId(currentItems);
    Map<String, SnapshotItem> previousItemsByProviderItemId = indexByProviderItemId(previousItems);

    List<SnapshotDiffItemResponse> addedItems =
        currentItems.stream()
            .filter(item -> !previousItemsByProviderItemId.containsKey(item.getProviderItemId()))
            .map(this::toDiffItemResponse)
            .toList();

    List<SnapshotDiffItemResponse> removedItems =
        previousItems.stream()
            .filter(item -> !currentItemsByProviderItemId.containsKey(item.getProviderItemId()))
            .map(this::toDiffItemResponse)
            .toList();

    List<SnapshotMovedItemResponse> movedItems =
        currentItems.stream()
            .filter(item -> previousItemsByProviderItemId.containsKey(item.getProviderItemId()))
            .filter(
                item ->
                    previousItemsByProviderItemId.get(item.getProviderItemId()).getPosition()
                        != item.getPosition())
            .map(
                item ->
                    toMovedItemResponse(
                        item, previousItemsByProviderItemId.get(item.getProviderItemId())))
            .toList();

    return new SnapshotDiffResponse(
        currentSnapshot.getId(), previousSnapshot.getId(), addedItems, removedItems, movedItems);
  }

  private Map<String, SnapshotItem> indexByProviderItemId(List<SnapshotItem> items) {
    return items.stream()
        .collect(Collectors.toMap(SnapshotItem::getProviderItemId, Function.identity()));
  }

  private SnapshotDiffItemResponse toDiffItemResponse(SnapshotItem item) {
    return new SnapshotDiffItemResponse(
        item.getProviderItemId(),
        item.getTitle(),
        item.getCreatorName(),
        item.getThumbnailUrl(),
        item.getPosition());
  }

  private SnapshotMovedItemResponse toMovedItemResponse(
      SnapshotItem currentItem, SnapshotItem previousItem) {
    return new SnapshotMovedItemResponse(
        currentItem.getProviderItemId(),
        currentItem.getTitle(),
        currentItem.getCreatorName(),
        currentItem.getThumbnailUrl(),
        previousItem.getPosition(),
        currentItem.getPosition());
  }
}
