package api.ipa.dto;

import api.ipa.entity.Paste;
import api.ipa.entity.User;
import api.ipa.entity.helpEntity.PasteVisibility;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public record UserPage(
        String username,
        String email,
        long totalViews,
        List<PastePreview> pastes
) {
    public static UserPage toUserPageWithAllData(User user) {
        return user.getPastes().stream()
                .collect(
                        Collectors.teeing(
                                Collectors.summingLong(Paste::getViews),

                                Collectors.mapping(PastePreview::toPastePreview, Collectors.toList()),

                                (totalPageViews, previews) ->
                                        new UserPage(
                                                user.getUsername(),
                                                user.getEmail(),
                                                totalPageViews,
                                                previews
                                        )
                        )
                );
    }

    public static UserPage toUserPageWithPublicData(User user) {
        return user.getPastes().stream()
                .filter(paste -> paste.getVisibility() == PasteVisibility.PUBLIC)
                .collect(
                        Collectors.teeing(
                                Collectors.summingLong(Paste::getViews),

                                Collectors.mapping(PastePreview::toPastePreview, Collectors.toList()),

                                (totalPageViews, previews) ->
                                        new UserPage(
                                                user.getUsername(),
                                                "",
                                                totalPageViews,
                                                previews
                                        )
                        )
                );
    }
}
