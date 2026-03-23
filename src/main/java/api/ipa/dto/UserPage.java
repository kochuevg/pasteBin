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
        String bio,
        String avatarURL,
        List<PastePreview> pastes
) {
    public static UserPage toUserPageWithAllData(User user, String fullAvatarPath) {
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
                                                user.getBio(),
                                                fullAvatarPath,
                                                previews
                                        )
                        )
                );
    }

    public static UserPage toUserPageWithPublicData(User user, String fullAvatarPath) {
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
                                                user.getBio(),
                                                fullAvatarPath,
                                                previews
                                        )
                        )
                );
    }
}
