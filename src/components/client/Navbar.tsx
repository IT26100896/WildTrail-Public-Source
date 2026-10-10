"use client";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
const LINKS = [
 {href:"/client/home",label:"Home"},{href:"/client/safaris",label:"Safaris"},
 {href:"/client/packages",label:"Packages"},{href:"/client/destinations",label:"Destinations"},
 {href:"/client/vehicles",label:"Vehicles"},{href:"/client/gallery",label:"Gallery"},
 {href:"/client/reviews",label:"Reviews"},{href:"/client/about",label:"About"},{href:"/client/contact",label:"Contact"},
];
export function Logo({ light = true }: { light?: boolean }) {
  return (
    <span className="flex items-center gap-2.5">
      <span className="grid h-9 w-9 place-items-center rounded-full border border-gold/60 bg-jungle-900 text-gold">
        <svg viewBox="0 0 24 24" className="h-5 w-5" fill="currentColor" aria-hidden>
          <path d="M12 2c-.6 2.6-2.4 4.1-4.6 5.1C5 8.2 3.5 10.4 3.5 13c0 4.7 3.8 8.5 8.5 8.5s8.5-3.8 8.5-8.5c0-2.6-1.5-4.8-3.9-5.9C14.4 6.1 12.6 4.6 12 2Zm0 6.5a1.6 1.6 0 1 1 0 3.2 1.6 1.6 0 0 1 0-3.2Zm-3.6 2a1.3 1.3 0 1 1 0 2.6 1.3 1.3 0 0 1 0-2.6Zm7.2 0a1.3 1.3 0 1 1 0 2.6 1.3 1.3 0 0 1 0-2.6ZM12 13c2 0 3.6 1.6 3.6 3.2 0 1.2-1 1.8-2 1.8-.6 0-1.1-.3-1.6-.3s-1 .3-1.6.3c-1 0-2-.6-2-1.8C8.4 14.6 10 13 12 13Z" />
        </svg>
      </span>
      <span className={`font-display text-xl font-bold tracking-[0.18em] ${light ? "text-white" : "text-jungle-950"}`}>
        WILD<span className="text-gold">TRAIL</span>
      </span>
    </span>
  );
}

export default function Navbar({ userName }: { userName: string | null }) {
 const pathname=usePathname();
 const [scrolled,setScrolled]=useState(false);
 const [openPath,setOpenPath]=useState<string|null>(null);
 const [dropdown,setDropdown]=useState<{path:string;kind:string}|null>(null);
 const [busy,setBusy]=useState(false);
 const [error,setError]=useState("");
 const open=openPath===pathname;
 const menu=dropdown?.path===pathname ? dropdown.kind : null;
 const isHome=["/","/client","/client/home"].includes(pathname);
 useEffect(()=>{const update=()=>setScrolled(window.scrollY>40);update();window.addEventListener("scroll",update,{passive:true});return()=>window.removeEventListener("scroll",update);},[]);
 useEffect(()=>{
  if(!open)return;
  const previous=document.body.style.overflow;document.body.style.overflow="hidden";
  const resize=()=>{if(window.innerWidth>=1100)setOpenPath(null);};
  window.addEventListener("resize",resize);
  return()=>{document.body.style.overflow=previous;window.removeEventListener("resize",resize);};
 },[open]);
 useEffect(()=>{const close=(e:KeyboardEvent)=>{if(e.key==="Escape"){setOpenPath(null);setDropdown(null);}};window.addEventListener("keydown",close);return()=>window.removeEventListener("keydown",close);},[]);
 async function logout(){setBusy(true);setError("");try{const response=await fetch("/api/auth/logout",{method:"POST"});if(!response.ok)throw new Error("Unable to sign out. Please try again.");window.location.replace("/client/login");}catch(e){setError((e as Error).message);setBusy(false);}}
 const linkClass=(href:string)=>`block whitespace-nowrap rounded-lg px-2 py-2 text-sm hover:bg-white/10 hover:text-gold ${pathname===href ? "text-gold" : "text-white/90"}`;
 const accountLinks=<><Link className={linkClass("/client/my-bookings")} href="/client/my-bookings">My Bookings</Link><Link className={linkClass("/client/my-reviews")} href="/client/my-reviews">My Reviews</Link><Link className={linkClass("/client/profile")} href="/client/profile">Profile</Link><button className="w-full rounded-lg px-3 py-2 text-left text-sm text-white hover:bg-white/10" disabled={busy} onClick={logout}>{busy?"Signing out…":"Sign out"}</button></>;
 return <>
  {menu && <button aria-label="Close dropdown" className="fixed inset-0 z-40 cursor-default" onClick={()=>setDropdown(null)}/>}
  <header className={`fixed inset-x-0 top-0 z-50 transition-all ${scrolled||!isHome||open?"bg-jungle-950/95 py-3 shadow-lg backdrop-blur-md":"bg-transparent py-5"}`}>
   <nav aria-label="Main navigation" className="mx-auto flex max-w-7xl items-center justify-between gap-3 px-5">
    <Link className="shrink-0" href="/client/home" aria-label="WildTrail home"><Logo/></Link>
    <div className="hidden items-center gap-1 min-[1100px]:flex">
     {LINKS.slice(0,5).map(l=><Link key={l.href} className={linkClass(l.href)} href={l.href}>{l.label}</Link>)}
     <div className="relative"><button aria-expanded={menu==="more"} aria-controls="more-links" className="px-3 py-2 text-sm text-white" onClick={()=>setDropdown(menu==="more"?null:{path:pathname,kind:"more"})}>More ▾</button>
      {menu==="more"&&<div id="more-links" className="absolute left-0 top-full min-w-44 rounded-xl bg-jungle-950 p-2 shadow-xl">{LINKS.slice(5).map(l=><Link key={l.href} href={l.href} className={linkClass(l.href)} onClick={()=>setDropdown(null)}>{l.label}</Link>)}</div>}
     </div>
    </div>
    <div className="hidden shrink-0 items-center gap-3 min-[1100px]:flex">
     {userName?<div className="relative"><button aria-expanded={menu==="account"} aria-controls="account-links" className="px-3 py-2 text-sm text-white" onClick={()=>setDropdown(menu==="account"?null:{path:pathname,kind:"account"})}>My account ▾</button>{menu==="account"&&<div id="account-links" className="absolute right-0 top-full w-48 rounded-xl bg-jungle-950 p-2 shadow-xl">{accountLinks}</div>}</div>:<Link href="/client/login" className="text-sm text-white hover:text-gold">Sign in</Link>}
     <Link href="/client/booking" className="btn-gold !px-4 !py-2.5 whitespace-nowrap">Book Now</Link>
    </div>
    <button className="h-10 w-10 shrink-0 text-2xl text-white min-[1100px]:hidden" aria-label={open?"Close menu":"Open menu"} aria-expanded={open} aria-controls="mobile-navigation" onClick={()=>setOpenPath(open?null:pathname)}>{open?"✕":"☰"}</button>
   </nav>
   {error&&<p role="alert" className="bg-red-800 px-5 py-2 text-sm text-white">{error}</p>}
  </header>
  {open&&<nav id="mobile-navigation" aria-label="Mobile navigation" className="fixed inset-0 z-40 overflow-y-auto overscroll-contain bg-jungle-950 px-6 pb-8 pt-24 min-[1100px]:hidden">
   {LINKS.map(l=><Link key={l.href} href={l.href} className={`${linkClass(l.href)} border-b border-white/10 !py-3 !text-lg`} onClick={()=>setOpenPath(null)}>{l.label}</Link>)}
   <div className="mt-5 space-y-2"><Link href="/client/booking" className="btn-gold w-full" onClick={()=>setOpenPath(null)}>Book Now</Link>{userName?accountLinks:<Link href="/client/login" className="btn-ghost w-full" onClick={()=>setOpenPath(null)}>Sign in</Link>}</div>
  </nav>}
 </>;
}
