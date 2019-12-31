package com.patchworkmc.mixin.networking;

import net.minecraftforge.fml.network.FMLLoginWrapper;
import net.minecraftforge.fml.network.ICustomPacket;
import net.minecraftforge.fml.network.NetworkDirection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.network.packet.LoginQueryResponseC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

@Mixin(LoginQueryResponseC2SPacket.class)
public class MixinLoginQueryResponseC2SPacket implements ICustomPacket<LoginQueryResponseC2SPacket> {
	@Shadow
	private PacketByteBuf response;

	@Shadow
	private int queryId;

	@Override
	public PacketByteBuf getInternalData() {
		return response;
	}

	@Override
	public Identifier getName() {
		// Forge: return FMLLoginWrapper.WRAPPER if there is no channel
		return FMLLoginWrapper.WRAPPER;
	}

	@Override
	public void setName(Identifier channelName) {
		// Forge: NO-OP if there is no channel
	}

	@Override
	public int getIndex() {
		return queryId;
	}

	@Override
	public void setIndex(int index) {
		this.queryId = index;
	}

	@Override
	public void setData(PacketByteBuf data) {
		this.response = data;
	}

	@Override
	public NetworkDirection getDirection() {
		return NetworkDirection.LOGIN_TO_CLIENT;
	}

	@Override
	public LoginQueryResponseC2SPacket getThis() {
		return (LoginQueryResponseC2SPacket) (Object) this;
	}
	@SuppressWarnings("PublicStaticMixinMember")
	@Invoker("<init>")
	public static LoginQueryResponseC2SPacket create() {
		throw new AssertionError("Mixin not applied");
	}
}
