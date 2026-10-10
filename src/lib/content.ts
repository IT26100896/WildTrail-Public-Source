export const px = (id: number, w = 1200, ext = "jpeg") =>
  `https://images.pexels.com/photos/${id}/pexels-photo-${id}.${ext}?auto=compress&cs=tinysrgb&w=${w}`;

export const IMG = {
  hero: px(10607669, 2000),
  leopardTree: px(17081253, 1400),
  elephantJungle: px(38593603, 1400),
  elephantElla: px(20321536, 1400),
  elephantsHerd: px(16159453, 1400),
  jeepRoad: px(16444264, 1400),
  jeepGrass: px(16444284, 1400),
  jeepGroup: px(16444281, 1400),
  sunset: px(23831844, 1600),
  sunsetBirds: px(12004886, 1400),
  lake: px(39339595, 1400),
  lagoon: px(37391961, 1400),
  peacock: px(36606292, 1000),
};

export const WILDLIFE = [
  { name: "Sri Lankan Leopard", latin: "Panthera pardus kotiya", img: px(10607669, 900), desc: "The island's apex predator — an endemic subspecies with no competing big cats.", where: "Yala, Wilpattu", best: "Feb – Jul, early morning" },
  { name: "Asian Elephant", latin: "Elephas maximus maximus", img: px(35671094, 900), desc: "Gentle giants roaming in herds, from lone tuskers to playful calves.", where: "Udawalawe, Minneriya", best: "All year · Aug–Oct gathering" },
  { name: "Sloth Bear", latin: "Melursus ursinus inornatus", img: px(37146984, 900), desc: "Shaggy, shy and elusive — often seen raiding palu fruit trees.", where: "Yala, Wilpattu", best: "May – Jul" },
  { name: "Spotted Deer", latin: "Axis axis ceylonensis", img: px(11004396, 900), desc: "Graceful herds that signal predators with sharp alarm calls.", where: "All parks", best: "All year" },
  { name: "Wild Buffalo", latin: "Bubalus arnee migona", img: px(6711724, 900), desc: "Massive grazers wallowing in waterholes through the heat of the day.", where: "Yala, Kumana", best: "Dry season" },
  { name: "Mugger Crocodile", latin: "Crocodylus palustris", img: px(32213843, 900), desc: "Ancient reptiles basking along tank edges and lagoon banks.", where: "Yala, Bundala", best: "Midday sun" },
  { name: "Indian Peafowl", latin: "Pavo cristatus", img: px(36606292, 900), desc: "Dazzling courtship displays echo across the dry zone each monsoon.", where: "Everywhere", best: "Nov – Jan displays" },
];

export const GALLERY = [
  { src: px(17081253, 1200), title: "Leopard at rest", tall: true },
  { src: px(16159453, 1200), title: "Elephant herd" },
  { src: px(16444281, 1200), title: "Safari jeeps" },
  { src: px(38593603, 1200), title: "Into the jungle", tall: true },
  { src: px(23831844, 1200), title: "Savanna sunset" },
  { src: px(36606292, 1200), title: "Peacock", tall: true },
  { src: px(16444264, 1200), title: "Safari group" },
  { src: px(39339595, 1200), title: "Dry-zone landscape" },
  { src: px(12004886, 1200), title: "Birds at dusk" },
  { src: px(3551497, 1200), title: "Elephant pair", tall: true },
  { src: px(37391961, 1200), title: "Kumana lagoon" },
  { src: px(10607669, 1200), title: "Leopard drinking" },
];

export const WHY = [
  { icon: "🧭", title: "Expert Trackers", text: "Registered guides and naturalists to help plan your wildlife experience." },
  { icon: "🚙", title: "Premium 4x4 Fleet", text: "Well-maintained open-top jeeps with comfortable seats, canopies and cooler boxes." },
  { icon: "🌿", title: "Responsible Safaris", text: "Respect wildlife, follow park rules and avoid disturbing animals." },
  { icon: "💳", title: "Easy, Secure Booking", text: "Reserve online and pay in cash on arrival. Staff record your payment." },
];

export const STEPS = [
  { n: "01", icon: "🗺️", title: "Choose Safari", href: "/client/safaris", text: "Pick a park and an experience that suits you." },
  { n: "02", icon: "📅", title: "Select Date", href: "/client/home#quick-booking", text: "Morning, evening or a full day in the wild." },
  { n: "03", icon: "🚙", title: "Choose Vehicle", href: "/client/vehicles", text: "From standard jeeps to luxury cruisers." },
  { n: "04", icon: "✅", title: "Make Booking", href: "/client/booking", text: "Review your reservation details and receive a booking reference." },
  { n: "05", icon: "🐆", title: "Enjoy The Wild", href: "/client/gallery", text: "We pick you up — you enjoy the adventure." },
];

export const FAQ = [
  { q: "What should I bring?", a: "Neutral-coloured light clothing, a hat, sunscreen, insect repellent, binoculars, a camera with a zoom lens and a light jacket for early mornings. We supply bottled water." },
  { q: "What time does the safari start?", a: "Morning safaris depart around 5:30 AM to reach the gate at opening (6:00 AM). Evening safaris start at about 2:00–2:30 PM and return after the park closes at 6:00 PM. Check the selected package for its exact departure and duration." },
  { q: "Can children join?", a: "Absolutely. Children of all ages are welcome, and our Family Safari includes child seats, snacks and a kids' activity pack. Children must be supervised by an adult at all times." },
  { q: "Can I cancel my booking?", a: "Yes. You can cancel free of charge from My Bookings up to 24 hours before your safari. If cash was already collected, contact staff to arrange its return." },
  { q: "What happens if it rains?", a: "Safaris operate in light rain — animals are often more active afterwards! If the park is closed by the authorities, we will reschedule or refund you in full." },
  { q: "Are park entrance tickets included?", a: "Park tickets are purchased at the gate on the day, as prices are set by the Department of Wildlife Conservation. Your tracker will assist with the process." },
  { q: "Do you offer hotel pickup?", a: "Yes, complimentary pickup and drop-off is included from hotels within 15 km of the park entrance." },
];

export const money = (n: number) => `$${n.toLocaleString("en-US", { minimumFractionDigits: 0, maximumFractionDigits: 2 })}`;

export const fmtDate = (d: string) =>
  new Date(d.length === 10 ? `${d}T00:00:00` : d).toLocaleDateString("en-GB", { day: "2-digit", month: "short", year: "numeric" });

export const fmtTime = (t: string) => {
  const [h, m] = t.split(":").map(Number);
  const ap = h >= 12 ? "PM" : "AM";
  return `${((h + 11) % 12) + 1}:${String(m).padStart(2, "0")} ${ap}`;
};

