/** JSON contracts returned by the Java API. No database or business logic runs in Next.js. */
export type Role = "ROLE_ADMIN" | "ROLE_CUSTOMER";
export interface SessionUser { uid: number; email: string; name: string; role: Role; exp: number }
export interface DestinationDTO { id: number; slug: string; name: string; location: string; description: string; bestWildlife: string; safariTypes: string; startingPrice: number; imageUrl: string }
export interface PackageDTO { id: number; name: string; destinationId: number; destinationName: string; durationHours: number; startTime: string; maxGuests: number; basePrice: number; description: string; imageUrl: string; active: boolean; type: "MORNING" | "EVENING" | "HALF_DAY" | "FULL_DAY" | "PRIVATE" | "FAMILY" | "PHOTOGRAPHY"; typeLabel: string; pricingNote: string; inclusions: string[] }
export interface VehicleDTO { id: number; name: string; registrationNumber: string | null; capacity: number; fourByFour: boolean; features: string[]; available: boolean; price: number; imageUrl: string; category: "STANDARD" | "HILUX" | "LUXURY" | "FAMILY"; categoryLabel: string; allFeatures: string[] }
// The joined booking response used by confirmation, customer history and staff views.
export interface BookingDetail {
  id: number;
  reference: string;
  safariDate: string;
  timeSlot: string;
  guests: number;
  totalAmount: number;
  status: string;
  specialRequests: string | null;
  createdAt: string;
  customerId: number;
  customerName: string;
  customerEmail: string;
  customerPhone: string | null;
  packageId: number;
  packageName: string;
  destinationName: string;
  vehicleId: number;
  vehicleName: string;
  paymentMethod: string | null;
  paymentStatus: string | null;
  transactionRef: string | null;
  customerRole: Role | null;
  vehicleNumber: string | null;
  pickupPlace: string | null;
  packageAmount: number | null;
  vehicleAmount: number | null;
  paymentAmount: number | null;
  paidAt: string | null;
  paymentCreatedAt: string | null;
}
export interface PaymentDetail { id: number; bookingId: number; method: string; amount: number; status: string; transactionRef: string; cardLast4: string | null; paidAt: string | null; createdAt: string; bookingReference: string; customerName: string; customerEmail: string; customerRole: Role | null; vehicleNumber: string | null; pickupPlace: string | null }
export interface ReviewReply { id: number; authorName: string; authorEmail?: string; comment: string; createdAt: string }
export interface ReviewRow { sample?: boolean; authorEmail?: string; authorRole?: Role; replies?: ReviewReply[]; id: number; authorName: string; country: string | null; rating: number; comment: string; createdAt: string; reviewType: string }
export interface DashboardStats { totalBookings: number; pendingBookings: number; totalCustomers: number; revenuePaid: number; revenuePending: number; activePackages: number; availableVehicles: number; publishedReviews: number; statusBreakdown: Record<string, number>; revenueByPackage: { name: string; total: number }[]; upcoming: BookingDetail[]; recent: BookingDetail[] }
