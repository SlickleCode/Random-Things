package lumien.randomthings.client.screen;

/** Implemented by the sprite buttons so {@link SpriteStateButton#renderTooltips} can ask the hovered one for its tooltip text. */
public interface TooltipButton
{
	String getTooltip();
}
