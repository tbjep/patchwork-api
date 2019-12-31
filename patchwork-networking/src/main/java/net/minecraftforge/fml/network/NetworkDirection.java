package net.minecraftforge.fml.network;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import net.minecraftforge.fml.LogicalSide;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.client.network.packet.LoginQueryRequestS2CPacket;
import net.minecraft.network.Packet;
import net.minecraft.server.network.packet.CustomPayloadC2SPacket;
import net.minecraft.server.network.packet.LoginQueryResponseC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import com.patchworkmc.mixin.networking.MixinCustomPayloadC2SPacket;
import com.patchworkmc.mixin.networking.MixinCustomPayloadS2CPacket;
import com.patchworkmc.mixin.networking.MixinLoginQueryRequestS2CPacket;
import com.patchworkmc.mixin.networking.MixinLoginQueryResponseC2SPacket;

public enum NetworkDirection {
	PLAY_TO_SERVER(NetworkEvent.ClientCustomPayloadEvent::new, LogicalSide.CLIENT, CustomPayloadC2SPacket.class, 1),
	PLAY_TO_CLIENT(NetworkEvent.ServerCustomPayloadEvent::new, LogicalSide.SERVER, CustomPayloadS2CPacket.class, 0),
	LOGIN_TO_SERVER(NetworkEvent.ClientCustomPayloadLoginEvent::new, LogicalSide.CLIENT, LoginQueryResponseC2SPacket.class, 3),
	LOGIN_TO_CLIENT(NetworkEvent.ServerCustomPayloadLoginEvent::new, LogicalSide.SERVER, LoginQueryRequestS2CPacket.class, 2);

	private static final Reference2ReferenceArrayMap<Class<? extends Packet>, NetworkDirection> packetLookup;

	static {
		packetLookup = Stream.of(values()).
			collect(Collectors.toMap(NetworkDirection::getPacketClass, Function.identity(), (m1, m2) -> m1, Reference2ReferenceArrayMap::new));
	}

	private final BiFunction<ICustomPacket<?>, Supplier<NetworkEvent.Context>, NetworkEvent> eventSupplier;
	private final LogicalSide logicalSide;
	private final Class<? extends Packet> packetClass;
	private final int otherWay;

	NetworkDirection(BiFunction<ICustomPacket<?>, Supplier<NetworkEvent.Context>, NetworkEvent> eventSupplier, LogicalSide logicalSide, Class<? extends Packet> clazz, int i) {
		this.eventSupplier = eventSupplier;
		this.logicalSide = logicalSide;
		this.packetClass = clazz;
		this.otherWay = i;
	}

	public static <T extends ICustomPacket<?>> NetworkDirection directionFor(Class<T> customPacket) {
		return packetLookup.get(customPacket);
	}

	private Class<? extends Packet> getPacketClass() {
		return packetClass;
	}

	public NetworkDirection reply() {
		return NetworkDirection.values()[this.otherWay];
	}

	public NetworkEvent getEvent(final ICustomPacket<?> buffer, final Supplier<NetworkEvent.Context> manager) {
		return this.eventSupplier.apply(buffer, manager);
	}

	public LogicalSide getOriginationSide() {
		return logicalSide;
	}

	public LogicalSide getReceptionSide() { return reply().logicalSide; }

	;

	public <T extends Packet<?>> ICustomPacket<T> buildPacket(Pair<PacketByteBuf, Integer> packetData, Identifier channelName) {
		ICustomPacket<T> packet = null;
		Class<? extends Packet> packetClass = getPacketClass();
		if(packetClass.equals(CustomPayloadC2SPacket.class)) {
			packet = (ICustomPacket<T>) MixinCustomPayloadC2SPacket.create();
		} else if (packetClass.equals(CustomPayloadS2CPacket.class)) {
			packet = (ICustomPacket<T>) MixinCustomPayloadS2CPacket.create();
		} else if (packetClass.equals(LoginQueryRequestS2CPacket.class)) {
			packet = (ICustomPacket<T>) MixinLoginQueryRequestS2CPacket.create();
		} else if (packetClass.equals(LoginQueryResponseC2SPacket.class)) {
			packet = (ICustomPacket<T>) MixinLoginQueryResponseC2SPacket.create();
		}
		packet.setName(channelName);
		packet.setData(packetData.getLeft());
		packet.setIndex(packetData.getRight());
		return packet;
	}
}
